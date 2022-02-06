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

package org.streeto;

import org.jetbrains.annotations.NotNull;
import org.streeto.scorers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.*;


public class CourseScorer {

    private final List<LegScorer> legScorers;
    private final ControlSiteFinder csf;

    public CourseScorer(StreetOPreferences preferences, ControlSiteFinder csf) {
        legScorers = List.of(
                new LegLengthScorer(preferences),
                new LegRouteChoiceScorer(preferences),
                new LegComplexityScorer(preferences),
                new BeenThisWayBeforeScorer(preferences),
                new TooCloseToAFutureControlScorer(preferences),
                new DogLegScorer(preferences),
                new DistinctControlSiteScorer(preferences, csf)
        );
        this.csf = csf;
    }

    public static double getOverallScore(List<Double> legScores) {
        return legScores.stream().mapToDouble(x -> Math.pow(x, 2)).average().orElse(0.0);
    }

    public List<Double> scoreLegs(List<ControlSite> controls) {
        var featureScores = getFeatureScoresPerLeg(controls);
        return featureScores != null ? getWeightedLegScores(featureScores) : scoreAsWorst(controls);
    }

    @NotNull
    private List<Double> scoreAsWorst(List<ControlSite> controls) {
        return controls.stream().map(x -> 0.0).collect(Collectors.toList());
    }

    @NotNull
    private List<Double> getWeightedLegScores(List<List<Double>> featureScores) {
        var weightedFeatureScores = mapIndexed(featureScores, (idx, scores) -> {
            var scorerWeighting = legScorers.get(idx).getWeighting();
            return scores.stream().map(score -> score * scorerWeighting).collect(Collectors.toList());
        }).collect(Collectors.toList());
        var weightedLegScores = transpose(weightedFeatureScores);
        var sumOfWeights = getSumOfWeights();
        return weightedLegScores.stream().map(ws -> ws.stream().mapToDouble(x -> x).sum() / sumOfWeights).collect(Collectors.toList());
    }

    private double getSumOfWeights() {
        return legScorers.stream().mapToDouble(LegScorer::getWeighting).sum();
    }

    private List<List<Double>> getFeatureScoresPerLeg(List<ControlSite> controls) {
        var legRoutes = windowed(controls, 2)
                .map(leg -> csf.findRoutes(first(leg).getLocation(), last(leg).getLocation()))
                .collect(Collectors.toList());


        var noPaths = legRoutes.stream()
                .anyMatch(it -> it.getAll().isEmpty() || it.getBest().hasErrors());
        if (noPaths)
            return null;
        else return legScorers.stream()
                .map(scorer -> scorer.apply(legRoutes))
                .collect(Collectors.toList());
    }

    public ScoreDetails score(List<ControlSite> controls) {
        var featureScores = getFeatureScoresPerLeg(controls);
        if (featureScores != null) {
            return new ScoreDetails(getWeightedLegScores(featureScores), getDetailedScores(featureScores));
        } else
            return null;
    }

    private Map<String, List<Double>> getDetailedScores(List<List<Double>> featureScores) {
        var names = legScorers.stream()
                .map(it -> it.getClass().getSimpleName())
                .collect(Collectors.toList());
        var results = new HashMap<String, List<Double>>();

        // side effects only folks - feeds the map
        forEachZipped(names, featureScores, results::put);
        return results;
    }
}