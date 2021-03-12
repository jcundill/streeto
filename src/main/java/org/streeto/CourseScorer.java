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

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;
import org.streeto.scorers.LegScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.*;


public class CourseScorer {

    private final List<LegScorer> legScorers;
    private final BiFunction<GHPoint, GHPoint, GHResponse> findRoutes;

    public CourseScorer(List<LegScorer> legScorers, BiFunction<GHPoint, GHPoint, GHResponse> findRoutes) {
        this.legScorers = legScorers;
        this.findRoutes = findRoutes;
    }

    public List<Double> scoreLegs(List<ControlSite> controls) {
        var featureScores = getFeatureScoresPerLeg(controls);
        return featureScores != null ? getAverageLegScore(featureScores) : scoreAsWorst(controls);
    }

    @NotNull
    private List<Double> scoreAsWorst(List<ControlSite> controls) {
        return controls.stream().map(x -> 0.0).collect(Collectors.toList());
    }

    @NotNull
    private List<Double> getAverageLegScore(List<List<Double>> featureScores) {
        var legScores = transpose(featureScores);
        return legScores.stream().map(this::average).collect(Collectors.toList());
    }

    private List<List<Double>> getFeatureScoresPerLeg(List<ControlSite> controls) {
        var legRoutes = windowed(controls, 2)
                .map(ab -> findRoutes.apply(ab.get(0).getLocation(), ab.get(1).getLocation()))
                .collect(Collectors.toList());
        if( legRoutes.stream().anyMatch(GHResponse::hasErrors)) return null;

        /*
                featureScores =
                        1       2       3       4       5       6
                FS1     0.1     0.2     0.1     0.1     0.5     0.1
                FS2     0.2     0.1     0.1     0.4     0.3     0.0
                FS3     0.3     0.1     0.2     0.0     0.0     0.4

                step.numberedControlScores = 0.2, 0.167, 0.167, 0.167, 0.267, 0.167
                featureScores =
         */
         return legScorers.stream()
                 .map(raw -> raw.score(legRoutes).stream().map(s -> s * raw.getWeighting()).collect(Collectors.toList())
         ).collect(Collectors.toList());
    }

    public ScoreDetails score(List<ControlSite> controls) {
        var featureScores = getFeatureScoresPerLeg(controls);
        if( featureScores != null) {
            return new ScoreDetails(getAverageLegScore(featureScores), getDetailedScores(featureScores));
        } else
            return null;
    }

    private double average(List<Double> scores) {
        return scores.stream().reduce(0.0, Double::sum) / scores.size();
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