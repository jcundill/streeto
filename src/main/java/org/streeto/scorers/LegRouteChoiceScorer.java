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
import org.streeto.csim.CSIM;
import org.streeto.csim.RouteSimilarity;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.*;

public class LegRouteChoiceScorer extends AbstractLegScorer {
    private final RouteSimilarity csim;

    public LegRouteChoiceScorer(StreetOPreferences preferences) {
        super(preferences);
        csim = new RouteSimilarity(preferences);
    }

    @Override
    public double getWeighting() {
        return preferences.getRouteChoiceWeighting();
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

    private double evaluate(GHResponse leg) {
        var score = 0.0;
        if (leg.hasAlternatives()) {
            score = evalAlts(leg);
        } else {
            score = 0.0;
        }
        return scoreFunction(score);
    }

    private double evalAlts(GHResponse leg) {
        var best = leg.getBest();
        var bestDistance = best.getDistance();
        var alts = drop(leg.getAll(), 1);
        var score = 0.0;
        for (ResponsePath alt : alts) {
            var similarity = csim.similarity(best, alt);
            var altScore = (bestDistance / alt.getDistance()) * (1.0 - similarity);
            score += altScore;
        }
        return score / alts.size();
    }

}
