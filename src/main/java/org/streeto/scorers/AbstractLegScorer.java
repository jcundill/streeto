package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.first;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;
import static org.streeto.utils.DistUtils.dist;

public abstract class AbstractLegScorer implements LegScorer {
    private final double weighting;

    protected AbstractLegScorer(double weighting) {
        this.weighting = weighting;
    }

    public double getWeighting() {
        return weighting;
    }

    protected List<GHPoint> getBestAsList(GHResponse a) {
        return iterableAsStream(a.getBest().getPoints()).collect(Collectors.toList());
    }

    protected List<GHPoint> getAsList(GHResponse a, int num) {
        return iterableAsStream(a.getAll().get(num).getPoints()).collect(Collectors.toList());
    }

    protected double getCommonRouteLength(List<GHPoint> first, List<GHPoint> second) {
        var commonLen = 0.0;
        int lastCommonStart = 0;
        boolean lastIsCommon = second.contains(first(first));
        for (int i = 1; i < first.size(); i++) {
            boolean isCommon = second.contains(first.get(i));
            if (isCommon && !lastIsCommon) {
                //startCommon section
                lastCommonStart = i;
            } else if (!isCommon && lastIsCommon) {
                //end common section
                commonLen += dist(first.get(lastCommonStart), first.get(i - 1));
            }
            lastIsCommon = isCommon;
        }
        return commonLen;
    }
}
