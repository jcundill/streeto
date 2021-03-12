package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;

import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.iterableAsStream;

public abstract class AbstractLegScorer implements LegScorer {

    public double getWeighting() {
        return weighting;
    }

    protected List<GHPoint> getBestAsList(GHResponse a) {
        return iterableAsStream(a.getBest().getPoints()).collect(Collectors.toList());
    }

    protected List<GHPoint> getAsList(GHResponse a, int num) {
        return iterableAsStream(a.getAll().get(num).getPoints()).collect(Collectors.toList());
    }

}
