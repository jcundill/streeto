package org.streeto.scorers;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;
import org.streeto.mapping.MapBox;
import org.streeto.mapping.MapFitter;
import org.streeto.utils.Envelope;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BetterMapScaleScorer extends AbstractLegScorer {

    @Override
    public List<Double> score(List<GHResponse> routedLegs) {
         Envelope env = new Envelope();
         routedLegs.forEach(leg -> leg.getAll().forEach(route -> route.getPoints().forEach(env::expandToInclude)));

        Optional<MapBox> obox = MapFitter.getForEnvelope(env);
        if (obox.isEmpty()) return fillScores(routedLegs, 0.0);
        var box = obox.get();
        var ratio = box.getScale() /15000.0 - 1.0/3.0;
        return fillScores(routedLegs, 1.0 - ratio / 2.0);
    }

    @NotNull
    private List<Double> fillScores(List<GHResponse> routedLegs, double v) {
        return routedLegs.stream().map(x -> v).collect(Collectors.toList());
    }

}
