/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.streeto;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;
import io.jenetics.util.RandomRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;


/**
 * Created by jcundill on 18/01/2017.
 */
public class ControlSiteFinder {

    private final GraphHopper gh;
    List<ControlSite> furniture;
    private final EdgeFilter filter;
    private final HashMap<List<GHPoint>, GHResponse> routedLegCache = new HashMap<>();
    private int hit = 0;
    private int miss = 0;
    private final Random rnd = RandomRegistry.random();


    public ControlSiteFinder(GraphHopper gh) {
        this.gh = gh;
        filter = DefaultEdgeFilter.allEdges(gh.getEncodingManager().getEncoder("streeto"));
    }

    public ControlSite findControlSiteNear(GHPoint point, double distance) {
        var node = findNearestControlSiteTo(getGHPointRelativeTo(point, randomBearing(), distance));
        while (node.isEmpty()) {
            node = findNearestControlSiteTo(getGHPointRelativeTo(point, randomBearing(), distance + ((rnd.nextDouble() - 0.5) * distance)));
        }
        return node.get();
    }

    public ControlSite findAlternativeControlSiteFor(ControlSite point, double distance) {
        var node = findNearestControlSiteTo(getGHPointRelativeTo(point.getLocation(), randomBearing(), rnd.nextDouble() * distance));
        while (node.isEmpty() || node.get() == point) {
            node = findNearestControlSiteTo(getGHPointRelativeTo(point.getLocation(), randomBearing(), rnd.nextDouble() * distance));
        }
        return node.get();
    }

    public void setFurniture(List<ControlSite> furniture) {
        this.furniture = furniture;
    }

    public GHResponse routeRequest(List<ControlSite> controls, int numAlternatives) {
        var req = new GHRequest(controls.stream().map(ControlSite::getLocation).collect(Collectors.toList()));
        return routeRequest(req, numAlternatives);
    }

    public GHResponse routeRequest(List<ControlSite> ctrls) {
        return routeRequest(ctrls, 0);
    }

    public GHResponse routeRequest(GHRequest req, int numAlternatives) {
        req.setWeighting("fastest");
        if (numAlternatives > 1) {
            req.setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
            req.getHints().put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.5);
        }
        return gh.route(req);
    }

    public GHResponse findRoutes(GHPoint from, GHPoint to) {
        var p = List.of(from, to);
        if (routedLegCache.containsKey(p)) {
            hit++;
            return routedLegCache.get(p);
        } else {
            miss++;
            var req = new GHRequest(from, to);
            req.setWeighting("fastest");
            req.setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
            req.getHints().put(Parameters.Algorithms.AltRoute.MAX_SHARE, 0.8);
            var resp = gh.route(req);
            routedLegCache.put(p, resp);
            return resp;
        }
    }

    public Optional<ControlSite> findNearestControlSiteTo(ControlSite site) {
        return findNearestControlSiteTo(site.getLocation());
    }

    public Optional<ControlSite> findNearestControlSiteTo(GHPoint p) {
        var loc = findClosestStreetLocation(p);
        if (loc.isEmpty()) return Optional.empty();
        else {
            // have we got nearby furniture - if so always use that
            var f = findLocalStreetFurniture(loc.get());
            if (f.isPresent()) return f;
            else {
                var site = loc.get();
                var isTower = gh.getLocationIndex().findClosest(site.lat, site.lon, filter).getSnappedPosition() == QueryResult.Position.TOWER;
                var desc = isTower ? "junction" : "bend";
                return Optional.of(new ControlSite(site, desc));
            }
        }
    }

    private Optional<ControlSite> findLocalStreetFurniture(GHPoint p) {
        var distance = 25.0;
        return furniture.stream().filter(it -> dist(it.getLocation(), p) < distance).findFirst();
    }

    private Optional<? extends GHPoint> findClosestStreetLocation(GHPoint p) {
        var qr = gh.getLocationIndex().findClosest(p.lat, p.lon, filter);

        if (!qr.isValid()) return Optional.empty();
        else if (qr.getSnappedPosition() == QueryResult.Position.EDGE) {
            var pl = qr.getClosestEdge().fetchWayGeometry(3);
            return iterableStreamOf(pl).filter(pt -> {
                        var loc = gh.getLocationIndex().findClosest(pt.lat, pt.lon, filter).getSnappedPosition();
                        return loc == QueryResult.Position.TOWER || loc == QueryResult.Position.PILLAR;
                    }
            ).findFirst();
        } else return Optional.of(qr.getSnappedPoint());

    }

    public GHPoint getGHPointRelativeTo(GHPoint loc, double bearing, double dist) {
        var radiusOfEarth = 6378.1 * 1000;//Radius of the Earth

        var lat1 = toRadians(loc.lat);
        var lon1 = toRadians(loc.lon);

        var lat2 = asin(sin(lat1) * cos(dist / radiusOfEarth) +
                        cos(lat1) * sin(dist / radiusOfEarth) * Math.cos(bearing));

        var lon2 = lon1 + atan2(Math.sin(bearing) * sin(dist / radiusOfEarth) * cos(lat1),
                cos(dist / radiusOfEarth) - sin(lat1) * sin(lat2));

        return new GHPoint(toDegrees(lat2), toDegrees(lon2));
    }

    public double randomBearing() {
        return 2 * PI * rnd.nextDouble();
    }
}
