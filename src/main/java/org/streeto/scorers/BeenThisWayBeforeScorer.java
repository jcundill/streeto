package org.streeto.scorers;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;
import static org.streeto.utils.CollectionHelpers.mapIndexed;

public class BeenThisWayBeforeScorer extends AbstractLegScorer {
    public BeenThisWayBeforeScorer(StreetOPreferences preferences) {
        super(preferences);
    }

    @Override
    public double getWeighting() {
        return preferences.getBeenHereBeforeWeighting();
    }

    @Override
    public List<Double> apply(List<GHResponse> routedLegs) {
        return mapIndexed(routedLegs, (idx, leg) -> evaluate(previousLegs(routedLegs, idx), leg))
                .collect(Collectors.toList());
    }

    @NotNull
    private List<GHResponse> previousLegs(List<GHResponse> routedLegs, Integer idx) {
        return routedLegs.subList(0, idx);
    }

    private double evaluate(List<GHResponse> previousLegs, GHResponse thisLeg) {
        // no legs other than the previous
        if (previousLegs.size() < 2) return 1.0;

        return previousLegs.stream()
                .map(l -> compareLegs(l, thisLeg))
                .min(Double::compareTo)
                .orElseThrow(NoSuchElementException::new);
    }

    private double compareLegs(GHResponse a, GHResponse b) {
        var score = 0.0;
        var pointsA = dropFirstAndLast(getBestAsList(a), 1);
        var pointsB = dropFirstAndLast(getBestAsList(b), 1);
        if (pointsA.isEmpty() || pointsB.isEmpty()) {
            score = 1.0;
        } else {
            var commonLen = getCommonRouteLength(pointsA, pointsB);
            // only care about the amount of repetition on this leg
            var commonRatio = commonLen / b.getBest().getDistance();
            score = 1.0 - commonRatio;
        }
        return scoreFunction(score);
    }

}
