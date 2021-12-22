package org.streeto.seeders;

import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.streeto.utils.CollectionHelpers.*;

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
        var unfiltered = Stream.concat(Stream.of(Optional.of(first(points))), xs);
        return unfiltered.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    protected List<ControlSite> generateInitialCourse(List<GHPoint> route, int requestedNumControls) {
        var points = route.stream().map(it -> csf.findControlSiteNear(it, 50.0)).collect(Collectors.toList());
        return fillFromInitialPoints(points, requestedNumControls);
    }

    protected List<GHPoint> merge(List<ControlSite> initialPoints, List<GHPoint> points, int requestedNumControls) {
        var existingPoints = dropFirstAndLast(initialPoints, 1).stream().map(ControlSite::getLocation);
        var spaces = requestedNumControls - initialPoints.size() - 2;
        Stream<GHPoint> extraPoints = spaces > 0 ? points.stream().limit(spaces) : Stream.empty();

        return Stream.of(Stream.of(first(initialPoints).getLocation()), existingPoints, extraPoints, Stream.of(last(initialPoints).getLocation())).flatMap(Function.identity()).toList();
    }
}