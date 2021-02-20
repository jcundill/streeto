package org.streeto.utils;

import com.graphhopper.util.DistancePlaneProjection;
import com.graphhopper.util.shapes.BBox;
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
}
