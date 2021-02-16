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
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class ComingBackHereLaterScorer extends AbstractLegScorer {
    /**
     * works out if we run through a future control on this leg
     * and scores it badly if we do
     */
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return mapIndexed(routedLegs, (idx, leg) -> {
            var futureLegs = routedLegs.subList(idx, routedLegs.size());
            return evaluate(futureLegs, leg);
        }).collect(Collectors.toList());
    }

    private double evaluate(List<GHResponse> futureLegs, GHResponse thisLeg) {
        if (futureLegs.isEmpty()) return 0.0; // no further legs
        else {
            var remainingControls = futureLegs.stream()
                    .map(this::getLastPoint)
                    .collect(Collectors.toList());
            if (iterableAsStream(thisLeg.getBest().getPoints())
                    .anyMatch(it -> goesTooCloseToAFutureControl(remainingControls, it))) return 1.0;
            else return 0.0;
        }
    }

    private GHPoint3D getLastPoint(GHResponse it) {
        return last(it.getBest().getPoints());
    }

    private boolean goesTooCloseToAFutureControl(List<? extends GHPoint> ctrls, GHPoint p) {
        return ctrls.stream().anyMatch(c -> dist(p, c) < 50.0 && dist(p, c) > 5.0);
    }
}