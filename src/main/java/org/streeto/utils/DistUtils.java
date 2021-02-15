package org.streeto.utils;

import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.shapes.GHPoint;

public class DistUtils {

    private static final DistancePlaneProjection dist2d = new DistancePlaneProjection();

    public static double dist(GHPoint a, GHPoint b) {
        return dist2d.calcDist(a.lat, a.lon, b.lat, b.lon);
    }

    public static double dist(double a, double b, double c, double d) {
        return dist2d.calcDist(a, b, c, d);
    }

}
