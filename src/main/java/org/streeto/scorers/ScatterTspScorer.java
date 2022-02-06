package org.streeto.scorers;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.StreetOPreferences;
import org.streeto.tsp.BestSubsetOfTsp;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static org.streeto.utils.CollectionHelpers.last;

public class ScatterTspScorer extends ControlSetScorer {
    private final BestSubsetOfTsp tsp;
    private final int requestedNumControls;
    private final int iterations;
    private final ControlSiteFinder csf;
    private final double requestedDistance;

    public ScatterTspScorer(StreetOPreferences preferences, ControlSiteFinder csf, int requestedNumControls, double requestedDistance, int iterations) {
        super(preferences);
        this.csf = csf;
        this.tsp = new BestSubsetOfTsp(csf);
        this.requestedNumControls = requestedNumControls;
        this.requestedDistance = requestedDistance;
        this.iterations = iterations;
    }

    @Override
    public double score(List<ControlSite> controls) {
        var best = tsp.solve(controls, requestedNumControls, iterations);
        if (best.isPresent()) {
            var vehicleRoute = best.get().getTourActivities().getJobs();
            //route.forEach(j -> System.out.println(j.getId()));
            var sites = Stream.of(Stream.of(controls.get(0)),
                    vehicleRoute.stream().map(j -> controls.get(j.getIndex())), Stream.of(last(controls))).flatMap(s -> s).toList();
            //System.out.println(sites);
            var distance = csf.routeRequest(sites, 0).getBest().getDistance();
            return 1.0 - abs(distance - requestedDistance) / requestedDistance;
        } else {
            return 0.0;
        }
    }
}
