package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.StreetOPreferences;
import org.streeto.utils.DistUtils;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;
import static org.streeto.utils.DistUtils.dist;

public abstract class AbstractLegScorer implements LegScorer {
    protected StreetOPreferences preferences;

    protected AbstractLegScorer(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    protected List<GHPoint> getBestAsList(GHResponse a) {
        return iterableAsStream(a.getBest().getPoints()).collect(Collectors.toList());
    }

    protected List<GHPoint> getRouteChoiceOptionAsList(GHResponse a, int num) {
        return iterableAsStream(a.getAll().get(num).getPoints()).collect(Collectors.toList());
    }

    protected double getCommonRouteLength(List<GHPoint> pointsPrior, List<GHPoint> pointsThis) {
        var commonLen = 0.0;
        // for all the points in this leg, see if they follow a course too close to any track segments on the prior leg
        for (int i = 1; i < pointsThis.size() && i < pointsPrior.size(); i++) {
            var distThisToPrior = nearestDistToATrackSegment(pointsThis.get(i), pointsPrior);
            var distPrevToPrior = nearestDistToATrackSegment(pointsThis.get(i - 1), pointsPrior);
            if (distThisToPrior < preferences.getMinControlSeparation() && distPrevToPrior < preferences.getMinControlSeparation()) {
                commonLen += dist(pointsThis.get(i - 1), pointsThis.get(i));
            }
        }
        return commonLen;
    }

    private double nearestDistToATrackSegment(GHPoint point, List<GHPoint> pointsPrior) {
        var minDist = Double.MAX_VALUE;
        for (int i = 1; i < pointsPrior.size(); i++) {
            var dist = DistUtils.getDistanceFromLine(pointsPrior.get(i - 1), pointsPrior.get(i), point);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    protected double scoreFunction(double score) {
        return pow(score, 2);
    }
}
