package org.streeto.csim;


import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

class Point {
    public double latitude;
    public double longitude;
    public long timestamp = 0;
    public boolean fake;
    public String code;

    public Point(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.fake = false;
    }

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = 0;
        this.fake = false;
    }

    public Point(double latitude, double longitude, String code) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = 0;
        this.fake = false;
        this.code = code;
    }

    public Point(double latitude, double longitude, boolean fake) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = 0;
        this.fake = fake;
    }

    public Point(String line) {
        String[] elements = line.split(" ");
        //
        latitude = Double.parseDouble(elements[0]);
        longitude = Double.parseDouble(elements[1]);
        if (elements.length == 3) {
            timestamp = Long.parseLong(elements[2].substring(0, elements[2].length() - 3));
        } else {
            timestamp = 0;
        }
        fake = false;
    }

    public Point(String latitude, String longitude) {
        this.latitude = Double.parseDouble(latitude);
        this.longitude = Double.parseDouble(longitude);
    }

    public String toString() {
        String resultString = "";
        resultString += "{\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}";
        return resultString;
    }

    public static Point fromGraphHopperPoint(GHPoint ghPoint) {
        return new Point(ghPoint.getLat(), ghPoint.getLon());
    }
}
