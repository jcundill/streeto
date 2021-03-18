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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;


public class DogLegScorer extends AbstractLegScorer {

    public DogLegScorer(StreetOPreferences preferences) {
        super(preferences.getDogLegWeighting());
    }

    /**
     * scores each numbered control based on the repetition of the route to it and the route from the previous control.
     * i.e. control 3 is in a bad place as the route from 1 to 2 is pretty much the same as the route from 2 to 3
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return dogLegs(routedLegs.stream().map(GHResponse::getBest).collect(Collectors.toList()));
    }

    List<Double> dogLegs(List<ResponsePath> routes) {
        return Stream.concat(Stream.of(1.0), windowed(routes,2).map(this::dogLegScore)).collect(Collectors.toList());
    }

    private Double dogLegScore(List<ResponsePath> previous2this2next) {
        var prev2this = previous2this2next.get(0);
        var this2next = previous2this2next.get(1);
        var prevPoints = prev2this.getPoints();
        var nextPoints = this2next.getPoints();
        if (prevPoints.size() < 2 || nextPoints.size() < 2) return 0.0; //controls are in the same place
        List<GHPoint3D> nextTail = drop(nextPoints, 1);
        var inBoth = dropLast(prevPoints, 1).stream().filter(nextTail::contains).collect(Collectors.toList());
        if (inBoth.size() == 0) return 1.0;
        else {
            var distInBoth = dist(first(inBoth), last(inBoth));
            return 1.0 - distInBoth / this2next.getDistance();
        }
    }
}

