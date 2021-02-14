package org.streeto.utils;

import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.DistancePlaneProjection;

public class DistUtils {

    private static final DistancePlaneProjection dist2d = new DistancePlaneProjection();

    public static double dist(GHPoint a, GHPoint b) {
        return dist2d.calcDist(a.lat, a.lon, b.lat, b.lon);
    }
}
