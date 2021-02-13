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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class LegLengthScorer extends AbstractLegScorer {

    /**
     * scores each leg just based on its length.
     * i.e. the second leg is bad as the route from 1 to 2 was too long
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        var averageLegLength = routedLegs.stream().mapToDouble(it -> it.getBest().getDistance()).sum() / routedLegs.size();
        var maxLegLength = 2.0 * averageLegLength;
        return routedLegs.stream().map(it -> evaluate(it, maxLegLength)).collect(Collectors.toList());
    }

    private double evaluate(GHResponse leg, double maxLegLength) {
        var best = leg.getBest().getDistance();
        double minLegLength = 20.0;
        if(best < minLegLength || best > maxLegLength)  return 1.0;
        else return 0.0;
    }

}
