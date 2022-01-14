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
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;
import org.streeto.csim.RouteSimilarityFinder;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class LegRouteChoiceScorer extends AbstractLegScorer {
    private final RouteSimilarityFinder csim;

    public LegRouteChoiceScorer(StreetOPreferences preferences) {
        super(preferences);
        csim = new RouteSimilarityFinder(preferences);
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
        var sortedDistances = leg.getAll().stream().sorted(Comparator.comparingDouble(ResponsePath::getDistance)).toList();
        // work out the delta between the length of the best and the length of the next best

        var best = sortedDistances.get(0);
        var nextBest = sortedDistances.get(1);
        if (best.getPoints().isEmpty() || nextBest.getPoints().isEmpty()) {
            return 0.0; // not a real choice
        } else {
            var lengthRatio = best.getDistance() / nextBest.getDistance();
            var similarity = csim.similarity(best, nextBest).getCsim();
            var scoringSimilarity = similarity;
            double maxAllowedShare = preferences.getMaxRouteShare();
            if (similarity < maxAllowedShare) {
                //in tolerance,  take the allowed segment off
                //don't go below 0.0 - for a tiny similarity and a big tolerance - all that choice is fine
                scoringSimilarity = max(0.0, similarity - (1.0 - maxAllowedShare));
            }
            return (1.0 - scoringSimilarity) * lengthRatio;
        }
    }
}
