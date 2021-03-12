package org.streeto;

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
        return 1.0 - legScores.stream().collect(Collectors.averagingDouble(it -> it));
    }

    private String formatName(String cls) {
        var a = cls.replaceAll("Scorer", "");
        if( a.length() > 15) {
            a = a.substring(0, 15);
        }

        return String.format("%-12s", a);

    }
    @Override
    public String toString() {
            var titles = new ArrayList<>(featureScores.keySet());
            var fScores = titles.stream().map(featureScores::get).collect(Collectors.toList());
            var header = titles.stream().map(title -> "\t" + formatName(title)).collect(Collectors.joining("", "Leg   Score     ", "\n"));
        var legDetails = transpose(fScores);
        var ret = new StringBuilder(header);
        forEachIndexed(legScores, (idx, score) ->
                ret.append(String.format("%2s:   %7f   %s\n", (idx + 1), 1.0 - score, legDetails.get(idx).stream().map(it -> String.format("  %7f      ", 1.0 - it)).collect(Collectors.joining("","", "")) )));

        return ret.toString();
    }

}