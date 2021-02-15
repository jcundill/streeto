package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import one.util.streamex.StreamEx;
import org.streeto.utils.CollectionHelpers;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLegScorer implements LegScorer {

    public double getWeighting() {
        return weighting;
    }

    protected List<GHPoint3D> getBestAsList(GHResponse a) {
        return StreamEx.of(a.getBest().getPoints().iterator()).toList();
    }

    protected List<GHPoint> getAsList(GHResponse a, int num) {
        return CollectionHelpers.streamFromPointList(a.getAll().get(num).getPoints()).collect(Collectors.toList());
    }

    protected List<? extends GHPoint> intersection(List<? extends GHPoint> pointsA, List<? extends GHPoint> pointsB) {
        return pointsA.stream()
                .filter(pointsB::contains)
                .collect(Collectors.toList());
    }
}
