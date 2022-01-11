/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint3D;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.getDistanceFromLine;

public class TooCloseToAFutureControlScorer extends AbstractLegScorer {

    public TooCloseToAFutureControlScorer(StreetOPreferences preferences) {
        super(preferences);

    }

    @Override
    public double getWeighting() {
        return preferences.getComesTooCloseWeighting();
    }

    /**
     * works out if we run through a future control on this leg
     * and scores it badly if we do
     */
    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        if (routedLegs.size() < 2) {
            return List.of(1.0);
        } else {
            //evaluate the start without including the finish
            var startScore = evaluate(dropLast(futureLegs(routedLegs, 0), 1), first(routedLegs));
            var restScore = drop(mapIndexed(routedLegs, (idx, leg) -> evaluate(futureLegs(routedLegs, idx), leg))
                    .collect(Collectors.toList()), 1);
            return Stream.concat(Stream.of(startScore), restScore.stream()).collect(Collectors.toList());
        }
    }

    @NotNull
    private List<GHResponse> futureLegs(List<GHResponse> routedLegs, int idx) {
        return routedLegs.subList(idx + 1, routedLegs.size());
    }

    private double evaluate(List<GHResponse> futureLegs, GHResponse thisLeg) {
        var score = 0.0;
        if (futureLegs.size() < 1) {
            score = 1.0; // no further legs
        } else {
            var remainingControls = futureLegs.stream()
                    .map(this::getLastPoint)
                    .collect(Collectors.toList());
            var mins = thisLeg.getAll().stream()
                    .map(legRoute -> scoreRoute(thisLeg.getBest().getDistance(), remainingControls, legRoute)).toList();
            score = Collections.min(mins);
        }
        return scoreFunction(score);
    }

    private Double scoreRoute(double bestDistance, List<GHPoint3D> remainingControls, ResponsePath legRoute) {
        var likelihood = legRoute.getDistance() < preferences.getCSIMCellSize() ? 1.0 : bestDistance / legRoute.getDistance();
        var points = legRoute.getPoints();
        var size = points.size();
        var minDist = Double.MAX_VALUE;
        for (GHPoint3D remainingControl : remainingControls) {
            for (int i = 1; i < size; i++) {
                var dist = getDistanceFromLine(points.get(i - 1), points.get(i), remainingControl);
                if (dist < minDist) {
                    minDist = dist;
                }
            }
        }
        if (minDist < preferences.getCSIMCellSize()) {
            return 1.0 - likelihood;
        } else {
            return 1.0;
        }
    }

    private GHPoint3D getLastPoint(GHResponse it) {
        return last(it.getBest().getPoints());
    }
}