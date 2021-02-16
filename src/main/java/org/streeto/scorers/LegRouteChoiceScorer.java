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
import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;
import org.streeto.utils.CollectionHelpers;

import java.util.List;
import java.util.stream.Collectors;

public class LegRouteChoiceScorer extends AbstractLegScorer {

    /**
     * scores each numbered control based on the route choice available in the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is to straightforward
     */
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return routedLegs.stream().map(this::evaluate).collect(Collectors.toList());
    }

    private Double evaluate(GHResponse leg) {
        if (leg.hasAlternatives()) return evalAlts(leg);
        else return 1.0;
    }

    private Double evalAlts(GHResponse leg) {
        List<PathWrapper> all = leg.getAll();
        var sorted = all.stream().map(PathWrapper::getDistance).sorted().collect(Collectors.toList());
        var num = sorted.size() - 1;
        var ratio = (sorted.get(num) - sorted.get(0)) / sorted.get(0);
        List<GHPoint> first = getAsList(leg, 0);
        List<GHPoint> second = getAsList(leg, num);
        var common = CollectionHelpers.intersection(first, second).size();
        var total = Math.min(first.size(), second.size());
        var commonRatio = common * 1.0 / total;

        if (commonRatio > 0.5 && ratio > 0.5) return 0.9;
        if (commonRatio > 0.5 && ratio < 0.2) return 0.75;
        return ratio;
    }
}
