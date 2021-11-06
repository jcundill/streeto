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
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;

public class LegRouteChoiceScorer extends AbstractLegScorer {

    public LegRouteChoiceScorer(StreetOPreferences preferences) {
        super(preferences.getRouteChoiceWeighting());
    }

    /**
     * scores each numbered control based on the route choice available in the previous leg.
     * i.e. control 2 is in a bad place as the route from 1 to 2 is too straightforward
     */
    @NotNull
    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        return routedLegs.stream().map(this::evaluate).collect(Collectors.toList());
    }

    private Double evaluate(GHResponse leg) {
        if (leg.hasAlternatives()) return evalAlts(leg);
        else return 0.0;
    }

    private Double evalAlts(GHResponse leg) {
        var sortedDistances = leg.getAll().stream().map(ResponsePath::getDistance).sorted().collect(Collectors.toList());
        // work out the delta between the length of the best and the length of the next best
        var ratio = (sortedDistances.get(1) - sortedDistances.get(0)) / sortedDistances.get(0);
        //work out how much these two routes have in common
        List<GHPoint> first = dropFirstAndLast(getAsList(leg, 0), 1);
        List<GHPoint> second = dropFirstAndLast(getAsList(leg, 1), 1);

        if (first.isEmpty() || second.isEmpty())
            return 0.0; // not a real choice

        var commonLen = getCommonRouteLength(first, second);
        var shortest = (first.size() < second.size()) ? leg.getAll().get(0).getDistance() : leg.getAll().get(1).getDistance();
        var commonRatio = commonLen / shortest;
        return 1.0 - (ratio + commonRatio);
    }

}
