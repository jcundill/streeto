package org.streeto.scorers;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.StreetOPreferences;
import org.streeto.tsp.BestSubsetOfTsp;
import org.streeto.tsp.OrienteeringProblemSolver;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static org.streeto.utils.CollectionHelpers.last;

public class ScatterTspScorer extends ControlSetScorer {
    private final int requestedNumControls;
    private final int iterations;
    private final ControlSiteFinder csf;
    private final double requestedDistance;

    public ScatterTspScorer(StreetOPreferences preferences, ControlSiteFinder csf, int requestedNumControls, double requestedDistance, int iterations) {
        super(preferences);
        this.csf = csf;
        this.requestedNumControls = requestedNumControls;
        this.requestedDistance = requestedDistance;
        this.iterations = iterations;
    }

    @Override
    public double score(List<ControlSite> controls) {
        var result = new OrienteeringProblemSolver(csf).solve(controls, requestedDistance, iterations);
         var distance = result.distance();
         var maxScore = controls.stream().mapToInt(ControlSite::getValue).sum();
         var scoreRatio = (result.score() * 1.0) / (maxScore * 1.0);
         return (1.0 - abs(distance - requestedDistance) / requestedDistance) * scoreRatio;
     }
}
