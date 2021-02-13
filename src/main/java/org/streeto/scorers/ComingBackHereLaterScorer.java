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
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.streeto.DistUtils.dist;

public class ComingBackHereLaterScorer extends AbstractLegScorer {
    /**
     * works out if we run through a future control on this leg
     * and scores it badly if we do
     */
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        AtomicInteger idx = new AtomicInteger();
        return StreamEx.of(routedLegs).map(leg -> {
            var futureLegs = routedLegs.subList(idx.incrementAndGet(), routedLegs.size());
            return evaluate(futureLegs, leg);
        }).toList();

    }

    private double evaluate(List<GHResponse> futureLegs, GHResponse thisLeg) {
            if(futureLegs.isEmpty()) return  0.0; // no further legs
            else {
                var remainingControls = futureLegs.stream()
                        .map(it -> {
                            var points = it.getBest().getPoints();
                            return points.get(points.getSize() - 1);
                        })
                        .collect(Collectors.toList());
                var legPointStream = StreamEx.of( thisLeg.getBest().getPoints().iterator());
                if(legPointStream.anyMatch( it -> goesTooCloseToAFutureControl(remainingControls, it))) return 1.0;
                else return 0.0;
                }
            }


    private boolean goesTooCloseToAFutureControl(List<? extends GHPoint> ctrls , GHPoint p) {
        return ctrls.stream().anyMatch(c -> dist(p, c) < 50.0 && dist(p, c) > 5.0 );
    }
}