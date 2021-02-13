package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLegScorer implements LegScorer{

    public double getWeighting() {
        return weighting;
    }

    protected List<GHPoint3D> getBestAsList(GHResponse a) {
        return StreamEx.of(a.getBest().getPoints().iterator()).toList();
    }

    protected List<GHPoint3D> getAsList(GHResponse a, int num) {
        return StreamEx.of(a.getAll().get(num).getPoints().iterator()).toList();
    }


    @NotNull
    protected List<GHPoint> intersection(List<GHPoint3D> pointsA, List<GHPoint3D> pointsB) {
        return pointsA.stream()
                .filter(pointsB::contains)
                .collect(Collectors.toList());
    }
}
