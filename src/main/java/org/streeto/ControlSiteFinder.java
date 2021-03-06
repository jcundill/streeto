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
import com.graphhopper.ResponsePath;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import io.jenetics.util.RandomRegistry;
import org.streeto.csim.RouteSimilarityFinder;
import org.streeto.csim.SimilarityResult;
import org.streeto.utils.Envelope;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;
import static org.streeto.utils.CollectionHelpers.windowed;
import static org.streeto.utils.DistUtils.dist;


/**
 * Created by jcundill on 18/01/2017.
 */
public class ControlSiteFinder {

    private final GraphHopper gh;
    private final EdgeFilter filter;
    private final Map<List<GHPoint>, GHResponse> routedLegCache = new HashMap<>();
    private final Map<GHPoint, Optional<ControlSite>> locationCache = new HashMap<>();
    private final Random rnd = RandomRegistry.random();
    private final StreetOPreferences preferences;
    private final RouteSimilarityFinder csim;
    private int lastCSIMCellSize;
    private double lastCSIMThreshold;
    List<ControlSite> furniture;
    private int hit = 0;
    private int miss = 0;

    public ControlSiteFinder(GraphHopperOSM gh, StreetOPreferences preferences) {
        this.gh = gh;
        filter = DefaultEdgeFilter.allEdges(gh.getEncodingManager().getEncoder("streeto"));
        this.preferences = preferences;
        this.csim = new RouteSimilarityFinder(preferences);
        this.lastCSIMCellSize = preferences.getCSIMCellSize();
        this.lastCSIMThreshold = preferences.getCSIMThreshold();
    }

    public Envelope getEnvelopeForProbableRoutes(List<ControlSite> controls) {
        var routes = windowed(controls, 2).map(it ->
                routeRequest(it).getBest()
        ).toList();

        var env = new Envelope();
        routes.forEach(it -> it.getPoints().forEach(env::expandToInclude));
        return env;
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
        if (numAlternatives > 1) {
            req.setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
            req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_SHARE, preferences.getMaxRouteShare());
            req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, numAlternatives);
        }
        req.setProfile("streeto");
        return gh.route(req);
    }

    private void checkRoutedLegCacheIsStillValid() {
        if (preferences.getCSIMCellSize() != lastCSIMCellSize || preferences.getCSIMThreshold() != lastCSIMThreshold) {
            this.lastCSIMCellSize = preferences.getCSIMCellSize();
            this.lastCSIMThreshold = preferences.getCSIMThreshold();
            this.routedLegCache.clear();
        }
    }

    public GHResponse findRoutes(GHPoint from, GHPoint to) {
        var p = List.of(from, to);
        checkRoutedLegCacheIsStillValid();
        if (routedLegCache.containsKey(p)) {
            hit++;
            return routedLegCache.get(p);
        } else {
            miss++;
            var req = new GHRequest(from, to);
            req.setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
            req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_SHARE, preferences.getMaxRouteShare());
            req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 10);
            req.setProfile("streeto");
            var resp = gh.route(req);
            if (resp.hasAlternatives()) {
                filterAlternatives(resp);
            }
            routedLegCache.put(p, resp);
            return resp;
        }
    }

    private void filterAlternatives(GHResponse resp) {
        var alts = resp.getAll();
        SimilarityResult[][] simArray = new SimilarityResult[alts.size()][alts.size()];
        //how similar are the alts to the other ones.
        for (int i = 0; i < alts.size(); i++) {
            for (int j = 0; j < alts.size(); j++) {
                simArray[i][j] = csim.similarity(alts.get(i), alts.get(j));
            }
        }
        var threshold = preferences.getCSIMThreshold();
        // filter out alternatives that are too similar to each other
        var toRemove = new HashSet<Integer>();
        for (int i = 0; i < alts.size(); i++) {
            for (int j = i + 1; j < alts.size(); j++) {
                if (simArray[i][j].getCsim() > threshold && simArray[j][i].getCsim() > threshold) {
                    // too similar, flag for removal
                    // remove the longer one
                    var longest = alts.get(i).getDistance() > alts.get(j).getDistance() ? i : j;
                    toRemove.add(longest);
                }
            }
        }
        // remove flagged alternatives
        var survivors = new ArrayList<ResponsePath>();
        for (int i = 0; i < alts.size(); i++) {
            if (!toRemove.contains(i)) {
                survivors.add(alts.get(i));
            }
        }
        resp.getAll().clear();
        resp.getAll().addAll(survivors);
    }

    public Optional<ControlSite> findNearestControlSiteTo(ControlSite site) {
        return findNearestControlSiteTo(site.getLocation());
    }

    public Optional<ControlSite> findNearestControlSiteTo(GHPoint p) {
        if (locationCache.containsKey(p)) {
            return locationCache.get(p);
        }

        Optional<ControlSite> nearest = Optional.empty();
        // have we got nearby furniture - if so always use that
        var f = findLocalStreetFurniture(p);
        if (f.isPresent()) {
            nearest = f;
        } else { // find the nearest TOWER or PILLAR on the map
            var loc = findClosestStreetLocation(p);
            if (loc.isEmpty()) nearest = Optional.empty();
            else {
                var site = loc.get();
                var isTower = gh.getLocationIndex().findClosest(site.lat, site.lon, filter).getSnappedPosition() == Snap.Position.TOWER;
                var desc = isTower ? "junction" : "bend";
                nearest = Optional.of(new ControlSite(site, desc, isTower ? ControlType.TOWER : ControlType.PILLAR));
            }
        }
        locationCache.put(p, nearest);
        return nearest;
    }

    private Optional<ControlSite> findLocalStreetFurniture(GHPoint p) {
        return furniture.stream().filter(it -> dist(it.getLocation(), p) < preferences.getMaxFurnitureDistance()).findFirst();
    }

    private Optional<? extends GHPoint> findClosestStreetLocation(GHPoint p) {
        var qr = gh.getLocationIndex().findClosest(p.lat, p.lon, filter);

        if (!qr.isValid()) return Optional.empty();
        else if (qr.getSnappedPosition() == Snap.Position.EDGE) {
            var pl = qr.getClosestEdge().fetchWayGeometry(FetchMode.ALL);
            return iterableAsStream(pl).filter(pt -> {
                        var loc = gh.getLocationIndex().findClosest(pt.lat, pt.lon, filter).getSnappedPosition();
                        return loc == Snap.Position.TOWER || loc == Snap.Position.PILLAR;
                    }
            ).findFirst();
        } else return Optional.of(qr.getSnappedPoint());

    }

    public PointList getWayGeometry(GHPoint p) {
        var qr = gh.getLocationIndex().findClosest(p.lat, p.lon, filter);
        return qr.getClosestEdge().fetchWayGeometry(FetchMode.ALL);

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
