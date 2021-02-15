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
import com.graphhopper.util.PointList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;


public class DogLegScorer extends AbstractLegScorer {

    /**
     * scores each numbered control based on the repetition of the route to it and the route from the previous control.
     * i.e. control 3 is in a bad place as the route from 1 to 2 is pretty much the same as the route from 2 to 3
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return dogLegs(routedLegs.stream().map(it -> it.getBest().getPoints()).collect(Collectors.toList()));
    }

    List<Double> dogLegs(List<PointList> routes) {
        if (routes.size() < 2) return List.of(0.0);
        else {
            var ret = new ArrayList<>(List.of(0.0));
            var dogLegScores = beforeAndAfterLegs(routes).map(this::dogLegScore).collect(Collectors.toList());
            ret.addAll(dogLegScores);
            return ret;
        }
    }

    private Double dogLegScore(List<PointList> legs) {
        var a2b = legs.get(0);
        var b2c = legs.get(1);
        if (a2b.size() < 2 || b2c.size() < 2) return 1.0; //controls are in the same place
        var inAandB = dropLast(a2b, 1).stream().filter(it -> drop(b2c, 1).contains(it)).collect(Collectors.toList());
        var numInAandB = inAandB.size();
        if (numInAandB == 0) return 0.0;
        else {
            var distInAandB = dist(first(inAandB), last(inAandB));

            if (distInAandB < 50.0) return 0.0;
            else if (distInAandB < 100.0) return 0.25;
            else if (distInAandB < 200.0) return 0.5;
            else return 1.0;
        }
    }
}

