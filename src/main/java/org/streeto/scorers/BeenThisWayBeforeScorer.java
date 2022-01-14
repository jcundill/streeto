package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.dropLast;
import static org.streeto.utils.CollectionHelpers.mapIndexed;
import static org.streeto.utils.DistUtils.dist;

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
        // no previous legs can't have been anywhere before
        if (previousLegs.isEmpty()) return 1.0;

        // the dogleg scorer evaluates the leg we just ran against this one
        return dropLast(previousLegs, 1).stream()
                .map(l -> compareLegs(l, thisLeg))
                .min(Double::compareTo)
                .orElse(1.0); // if there are no previous legs, we can't have been anywhere before
    }

    private double compareLegs(GHResponse priorLeg, GHResponse thisLeg) {
        var score = 0.0;
        var pointsPrior = getBestAsList(priorLeg);
        var pointsThis = getBestAsList(thisLeg);
        if (pointsPrior.isEmpty() || pointsThis.isEmpty() || thisLeg.getBest().getDistance() == 0) {
            score = 0.0;
        } else {
            var commonLen = getCommonRouteLength(pointsPrior, pointsThis);
            // only care about the amount of repetition on this leg
            var commonRatio = commonLen / thisLeg.getBest().getDistance();
            score = 1.0 - commonRatio;
        }
        return scoreFunction(score);
    }

    private double getCommonRouteLength(List<GHPoint> pointsPrior, List<GHPoint> pointsThis) {
        var commonLen = 0.0;
        // for all the points in this leg, see if they follow a course too close to any track segments on the prior leg
        for (int i = 1; i < pointsThis.size() && i < pointsPrior.size(); i++) {
            var distThisToPrior = nearestDistToATrackSegment(pointsThis.get(i), pointsPrior);
            var distPrevToPrior = nearestDistToATrackSegment(pointsThis.get(i - 1), pointsPrior);
            if (distThisToPrior < preferences.getCSIMCellSize() && distPrevToPrior < preferences.getCSIMCellSize()) {
                commonLen += dist(pointsThis.get(i - 1), pointsThis.get(i));
            }
        }
        return commonLen;
    }
}

