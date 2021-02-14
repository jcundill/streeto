package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint3D;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

public class BeenThisWayBeforeScorer extends AbstractLegScorer {
    @NotNull
    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
        AtomicInteger idx = new AtomicInteger();
        return StreamEx.of(routedLegs).map(leg -> {
            var result = evaluate(routedLegs.subList(0, idx.get()), leg);
            idx.addAndGet(1);
            return result;
        }).toList();
    }


    private double evaluate(List<GHResponse> previousLegs, GHResponse thisLeg) {
        // no legs other than the previous
        if (previousLegs.size() < 2) return 0.0;

        var xs = previousLegs.stream().map(l -> compareLegs(l, thisLeg)).collect(Collectors.toList());
        if (xs.isEmpty()) return 0.0;
        else {
            return xs.stream()
                    .max(Double::compareTo)
                    .orElseThrow(NoSuchElementException::new);
        }
    }

    private double compareLegs(GHResponse a, GHResponse b) {
        var pointsA = removeStartAndFinish(getBestAsList(a));
        var pointsB = removeStartAndFinish(getBestAsList(b));
        if (pointsA.isEmpty() || pointsB.isEmpty()) return 0.0;
        else {
            var result = intersection(pointsA, pointsB);
            return result.size() * 1.0 / min(pointsB.size(), pointsA.size());

        }

    }

    private List<GHPoint3D> removeStartAndFinish(List<GHPoint3D> points) {
        var last = points.size() - 1;
        if (last < 1) return List.of();
        return points.subList(1, last);
    }

}
