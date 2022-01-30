package org.streeto.csim;


import com.graphhopper.util.shapes.GHPoint;

class Point {
    private final double latitude;
    private final double longitude;
    private final boolean fake;
    private String code;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.fake = false;
    }

    public Point(double latitude, double longitude, String code) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.fake = false;
        this.code = code;
    }

    public String toString() {
        return "{\"latitude\":%s,\"longitude\":%s}".formatted(latitude, longitude);
    }

    public static Point fromGraphHopperPoint(GHPoint ghPoint) {
        return new Point(ghPoint.getLat(), ghPoint.getLon());
    }

    public boolean isFake() {
        return fake;
    }

    public String getCode() {
        return code;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
