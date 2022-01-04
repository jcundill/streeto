package org.streeto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.forEachIndexed;
import static org.streeto.utils.CollectionHelpers.transpose;

public class ScoreDetails {
    private final List<Double> legScores;
    private final Map<String, List<Double>> featureScores;

    public ScoreDetails(List<Double> legScores, Map<String, List<Double>> featureScores) {

        this.legScores = legScores;
        this.featureScores = featureScores;
    }

    public List<Double> getLegScores() {
        return legScores;
    }

    public Map<String, List<Double>> getFeatureScores() {
        return featureScores;
    }

    public double getOverallScore() {
        return CourseScorer.getOverallScore(legScores);
    }

    private String formatName(String cls) {
        var a = cls.replaceAll("Scorer", "");
        if (a.length() > 15) {
            a = a.substring(0, 15);
        }
        return String.format("%-12s", a);

    }

    @Override
    public String toString() {
        List<List<Double>> legDetails = getLegDetails();
        var titles = new ArrayList<>(featureScores.keySet());
        var header = titles.stream().map(title -> "\t" + formatName(title)).collect(Collectors.joining("", "Leg   Score     ", "\n"));
         var ret = new StringBuilder(header);
        forEachIndexed(legScores, (idx, score) ->
                ret.append(String.format("%2s:   %7f   %s\n", (idx + 1), score, legDetails.get(idx).stream().map(it -> String.format("  %7f      ", it)).collect(Collectors.joining("", "", "")))));

        return ret.toString();
    }

    @NotNull
    public List<List<Double>> getLegDetails() {
        var titles = new ArrayList<>(featureScores.keySet());
        var fScores = titles.stream().map(featureScores::get).collect(Collectors.toList());
        return transpose(fScores);
    }

}