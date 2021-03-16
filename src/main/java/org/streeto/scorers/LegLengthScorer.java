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
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.mapIndexed;

public class LegLengthScorer extends AbstractLegScorer {

    private final double minLegLength;
    private final double maxLastLegLength;
    private final double maxFirstLegLength;

    public LegLengthScorer(StreetOPreferences preferences) {
        super(preferences.getLegLengthWeighting());
        this.minLegLength = preferences.getMinLegLength();
        this.maxLastLegLength = preferences.getMaxLastLegLength();
        this.maxFirstLegLength = preferences.getMaxFirstControlDistance();
    }

    /**
     * scores each leg just based on its length.
     * i.e. the second leg is bad as the route from 1 to 2 was too long
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        var averageLegLength = routedLegs.stream().mapToDouble(it -> it.getBest().getDistance()).sum() / routedLegs.size();
        var maxLegLength = Math.min(1000.0, 3.0 * averageLegLength);
        return mapIndexed(routedLegs, (idx, leg) -> {
            var len = idx == 0 ? maxFirstLegLength : idx == routedLegs.size() - 1 ? maxLastLegLength : maxLegLength;
            return evaluate(leg, len);
        }).collect(Collectors.toList());
    }

    private double evaluate(GHResponse leg, double maxLegLength) {
        var best = leg.getBest().getDistance();
         if (best < minLegLength || best > maxLegLength) return 0.0;
        else return 1.0;
    }
}
