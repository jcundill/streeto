package org.streeto.csim;

import org.opensextant.geodesy.Ellipsoid;
import org.opensextant.geodesy.Latitude;
import org.opensextant.geodesy.Longitude;
import org.opensextant.geodesy.MGRS;

public class MGRSConverter {
    public static MGRSCell convert(double lat, double lon) {
        var latAngle = new Latitude(Math.toRadians(lat));
        var lonAngle = new Longitude(Math.toRadians(lon));
        var mgrs = new MGRS(Ellipsoid.getInstance("WGS 84"), lonAngle, latAngle);
        var string = mgrs.toString(5);
        return new MGRSCell(string);
    }

    public static Point convertToLatLon(String mgrsString) {
        var mgrs = new MGRS(mgrsString);
        var geo = mgrs.toGeodetic2DPoint();
        var lat = geo.getLatitude().inDegrees();
        var lon = geo.getLongitude().inDegrees();
        return new Point(lat, lon);
    }

    public static class MGRSCell {
        public final int x;
        public final int y;
        public final String zone;

        public MGRSCell(String mgrsString) {
            this.zone = mgrsString.substring(0, 5);
            this.x = Integer.parseInt(mgrsString.substring(5, 10));
            this.y = Integer.parseInt(mgrsString.substring(10, 15));
        }

        public String getCode(String separator) {
            return String.format("%s%s%05d%s%05d", zone, separator, x, separator, y);
        }
    }


}
