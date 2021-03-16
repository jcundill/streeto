package org.streeto.utils;

import com.graphhopper.util.shapes.GHPoint;

public class Envelope {

    double minLat = 100.0;
    double maxLat = -100.0;
    double minLon = 370.0;
    double maxLon = -370.0;

    public void expandToInclude(GHPoint p) {
        if (p.lat < minLat) minLat = p.lat;
        if (p.lat > maxLat) maxLat = p.lat;
        if (p.lon < minLon) minLon = p.lon;
        if (p.lon > maxLon) maxLon = p.lon;
    }

    public double getWidth() {
        return maxLon - minLon;
    }

    public double getHeight() {
        return maxLat - minLat;
    }

    public GHPoint centre() {
        var centreLat = (maxLat - minLat) / 2.0 + minLat;
        var centreLon = (maxLon - minLon) / 2.0 + minLon;
        return new GHPoint(centreLat, centreLon);
    }
}
