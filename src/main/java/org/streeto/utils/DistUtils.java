package org.streeto.utils;

import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.shapes.GHPoint;

import static java.lang.Math.*;

public class DistUtils {

    private static final DistancePlaneProjection dist2d = new DistancePlaneProjection();

    public static double dist(GHPoint a, GHPoint b) {
        return dist2d.calcDist(a.lat, a.lon, b.lat, b.lon);
    }

    public static double dist(double a, double b, double c, double d) {
        return dist2d.calcDist(a, b, c, d);
    }

    public static GHPoint degreesToMetres(double lon, double lat) {
        // https://gist.github.com/springmeyer/871897
        var x = lon * 20037508.34 / 180;
        var y = log(tan((90 + lat) * PI / 360)) / (PI / 180);
        y = y * 20037508.34 / 180;
        return new GHPoint(y, x);
    }


    public static double getDistanceFromLine(GHPoint pt1, GHPoint pt2, GHPoint p) {
        var closest = getClosestPoint(pt1, pt2, p);
        return dist(closest, p);
    }

    public static boolean hasNormal(GHPoint pt1, GHPoint pt2, GHPoint p) {
        double u = ((p.lon - pt1.lon) * (pt2.lon - pt1.lon) + (p.lat - pt1.lat) * (pt2.lat - pt1.lat)) /
                   (pow(pt2.lat - pt1.lat, 2) + pow(pt2.lon - pt1.lon, 2));
        return u > 0.0 && u < 1.0;
    }

    public static GHPoint getClosestPoint(GHPoint pt1, GHPoint pt2, GHPoint p) {
        double u = ((p.lon - pt1.lon) * (pt2.lon - pt1.lon) + (p.lat - pt1.lat) * (pt2.lat - pt1.lat)) /
                   (pow(pt2.lat - pt1.lat, 2) + pow(pt2.lon - pt1.lon, 2));

        if (u > 1.0)
            return pt2;
        else if (u <= 0.0)
            return pt1;
        else {
            double x = (pt2.lon * u + pt1.lon * (1.0 - u));
            double y = (pt2.lat * u + pt1.lat * (1.0 - u));
            return new GHPoint(y, x);
        }
    }

    public static double getSlope(GHPoint pt1, GHPoint pt2) {
        return (pt2.lat - pt1.lat) / (pt2.lon - pt1.lon);
    }

}
