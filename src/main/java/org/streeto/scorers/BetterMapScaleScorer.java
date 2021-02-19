package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.jetbrains.annotations.NotNull;
import org.streeto.mapping.MapBox;
import org.streeto.mapping.MapFitter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BetterMapScaleScorer extends AbstractLegScorer {

    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
         Envelope env = new Envelope();
         routedLegs.forEach(leg -> leg.getAll().forEach(route -> route.getPoints().forEach(p -> env.expandToInclude(new Coordinate(p.lat, p.lon)))));

        Optional<MapBox> obox = MapFitter.getForEnvelope(env);
        if (obox.isEmpty()) return getScores(routedLegs, 1.0);
        var box = obox.get();
        var ratio = box.getScale() /15000.0 - 1.0/3.0;
        return getScores(routedLegs, ratio /2.0);
    }

    @NotNull
    private List<Double> getScores(List<GHResponse> routedLegs, double v) {
        return scoreLegs(routedLegs, v).collect(Collectors.toList());
    }

    @NotNull
    private Stream<Double> scoreLegs(List<GHResponse> routedLegs, double amount) {
        return routedLegs.stream().map(x -> amount);
    }
}
