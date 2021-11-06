package org.streeto.gpx;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Route;
import io.jenetics.jpx.WayPoint;
import io.jenetics.jpx.geom.Geoid;
import org.streeto.ControlSite;
import org.streeto.Course;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.streeto.utils.CollectionHelpers.iterableAsStream;

public class GpxFacade {

    public static Course readCourse(String filename) throws IOException {
        var is = new FileInputStream(filename);
        var gpx = GPX.reader(GPX.Reader.Mode.STRICT).read(is);
        var route = gpx.routes()
                .map(GpxFacade::toCourseControls)
                .findFirst()
                .orElseThrow();
        var dist = gpx.getTracks().get(0).getSegments().get(0).getPoints().stream().collect(Geoid.DEFAULT.toTourLength()).doubleValue();
        return new Course(dist, route.size() - 2, route);
    }

    private static List<ControlSite> toCourseControls(Route route) {
        return route.points()
                .map(GpxFacade::toControlSite)
                .collect(Collectors.toList());
    }

    private static ControlSite toControlSite(WayPoint wayPoint) {
        return new ControlSite(wayPoint.getLatitude().doubleValue(),
                wayPoint.getLongitude().doubleValue(),
                wayPoint.getName().orElse(""),
                wayPoint.getDescription().orElse(""));
    }

    public static void writeCourse(File filename, ResponsePath pathWrapper, List<ControlSite> controls) throws IOException {
        var points = iterableAsStream(pathWrapper
                .getPoints())
                .map(GpxFacade::toWayPoint)
                .collect(Collectors.toList());
        var ctrls = controls.stream()
                .map(GpxFacade::toWayPoint).collect(Collectors.toList());
        var route = toRoute(ctrls);
        var gpxBuilder = GPX.builder()
                .addTrack(track -> track
                        .name("Best Track")
                        .addSegment(s -> s.points(points)))
                .addRoute(route)
                .creator("StreetO");
        ctrls.forEach(gpxBuilder::addWayPoint);
        var gpx = gpxBuilder.build();


        GPX.writer("    ")
                .write(gpx, filename);

    }

    private static Route toRoute(List<WayPoint> controls) {
        var builder = Route.builder();
        controls.forEach(builder::addPoint);
        return builder.build();
    }

    private static WayPoint toWayPoint(GHPoint p) {
        return WayPoint.builder().lat(p.lat).lon(p.lon).build();
    }

    private static WayPoint toWayPoint(ControlSite site) {
        return WayPoint.builder()
                .lat(site.getLocation().lat)
                .lon(site.getLocation().lon)
                .name(site.getNumber())
                .desc(site.getDescription())
                .build();
    }

    public static void main(String[] args) {
        try {
            var course = readCourse("/home/jon/code/streeto/abc.gpx");
            System.out.println(course);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
