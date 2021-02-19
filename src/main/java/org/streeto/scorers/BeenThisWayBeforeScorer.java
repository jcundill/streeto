package org.streeto.scorers;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static org.streeto.utils.CollectionHelpers.*;

public class BeenThisWayBeforeScorer extends AbstractLegScorer {
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        return mapIndexed(routedLegs, (idx, leg) -> evaluate(previousLegs(routedLegs, idx), leg))
                .collect(Collectors.toList());
    }

    @NotNull
    private List<GHResponse> previousLegs(List<GHResponse> routedLegs, Integer idx) {
        return routedLegs.subList(0, idx);
    }

    private double evaluate(List<GHResponse> previousLegs, GHResponse thisLeg) {
        // no legs other than the previous
        if (previousLegs.size() < 2) return 0.0;

        var xs = previousLegs.stream()
                .map(l -> compareLegs(l, thisLeg))
                .collect(Collectors.toList());

        return xs.stream()
                .max(Double::compareTo)
                .orElseThrow(NoSuchElementException::new);

    }

    private double compareLegs(GHResponse a, GHResponse b) {
        var pointsA = dropFirstAndLast(getBestAsList(a), 1);
        var pointsB = dropFirstAndLast(getBestAsList(b), 1);
        if (pointsA.isEmpty() || pointsB.isEmpty()) return 0.0;
        else {
            var result = intersection(pointsA, pointsB);
            return result.size() * 1.0 / min(pointsB.size(), pointsA.size());
        }
    }
}
