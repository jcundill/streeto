package org.streeto.seeders;

import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

abstract class AbstractSeeder implements SeedingStrategy {
    protected static final double twistFactor = 0.67; //probable crow files disance vs actual distance
    private final ControlSiteFinder csf;

    AbstractSeeder(ControlSiteFinder csf) {
        this.csf = csf;
    }

    public ControlSiteFinder getCsf() {
        return csf;
    }

    private List<ControlSite> fillFromInitialPoints(List<ControlSite> points, int requestedNumControls) {
        var pointList = csf.routeRequest(points).getBest().getPoints();
        var xs = IntStream.range(1, requestedNumControls)
                .mapToObj(it -> {
                    var position = pointList.get(it * (pointList.size() / requestedNumControls - 1));
                    return csf.findNearestControlSiteTo(position);
                });
        var unfiltered = xs.collect(Collectors.toList());
        return unfiltered.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    protected List<ControlSite> generateInitialCourse(List<GHPoint> route, int requestedNumControls) {
        var points = route.stream().map(it -> csf.findControlSiteNear(it, 50.0)).collect(Collectors.toList());
        return fillFromInitialPoints(points, requestedNumControls);
    }
}