package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.StreetOPreferences;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.pow;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;
import static org.streeto.utils.DistUtils.getDistanceFromLine;

public abstract class AbstractLegScorer implements LegScorer {
    protected StreetOPreferences preferences;

    protected AbstractLegScorer(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    protected List<GHPoint> getBestAsList(GHResponse a) {
        return iterableAsStream(a.getBest().getPoints()).collect(Collectors.toList());
    }

    protected double nearestDistToATrackSegment(GHPoint point, List<GHPoint> pointsPrior) {
        var minDist = Double.MAX_VALUE;
        for (int i = 1; i < pointsPrior.size(); i++) {
            var dist = getDistanceFromLine(pointsPrior.get(i - 1), pointsPrior.get(i), point);
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
