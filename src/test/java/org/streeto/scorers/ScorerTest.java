package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.streeto.ControlSite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.streeto.utils.CollectionHelpers.windowed;
import static org.streeto.utils.DistUtils.dist;

public interface ScorerTest {

    default ControlSite toControlSite(WayPoint wayPoint) {
        var loc = new GHPoint(wayPoint.getLatitude().doubleValue(),
                wayPoint.getLongitude().doubleValue());
        var site = new ControlSite(loc, wayPoint.getDescription().orElse(""));
        site.setNumber(wayPoint.getName().orElse(""));
        return site;
    }

    default  GHPoint toGHPoint(WayPoint wayPoint) {
        var loc = new GHPoint(wayPoint.getLatitude().doubleValue(),
                wayPoint.getLongitude().doubleValue());
        return loc;
    }

    default List<ControlSite> gpxToControlSites(String name) throws IOException {
        var is = this.getClass().getResourceAsStream(name);
        var gpx = GPX.reader(GPX.Reader.Mode.STRICT).read(is);
        return gpx.getWayPoints().stream().map(this::toControlSite).toList();
    }

    default List<GHResponse> gpxToGhResponses(String name) throws IOException {
        var is = this.getClass().getResourceAsStream(name);
        var gpx = GPX.reader(GPX.Reader.Mode.STRICT).read(is);
        var track = gpx.getTracks().get(0);

        var points = track.getSegments().get(0).getPoints();
        var route = points.stream().map(this::toGHPoint).toList();

        var ctrls = gpx.getWayPoints().stream().map(this::toControlSite).toList();

        // split to legs
        var legs = new ArrayList<List<GHPoint>>();
        var lastPos = 0;
        var currLeg = 1;
        for( int i = 1; i < route.size(); i++) {
            var pt = route.get(i);
            var nextCtrl = ctrls.get(currLeg);
            if( pt.lat == nextCtrl.getLocation().lat && pt.lon == nextCtrl.getLocation().lon) {
                var leg = route.subList(lastPos , i);
                legs.add(leg);
                lastPos = i;
                currLeg++;
            }
        }

        var routedLegs = legs.stream().map(leg -> {
            var responsePath = new ResponsePath();
            var pl = new PointList();
            leg.forEach(pl::add);
            responsePath.setPoints(pl);
            var ghResponse = new GHResponse();
            ghResponse.add(responsePath);
            return ghResponse;
        }).toList();

        routedLegs.forEach(ghResponse -> {
            var responsePath = ghResponse.getBest();
            var pl = responsePath.getPoints();
            responsePath.setDistance( windowed(pl, 2).mapToDouble(pts -> dist(pts.get(0), pts.get(1))).sum());
        });
        return routedLegs;
    }

}
