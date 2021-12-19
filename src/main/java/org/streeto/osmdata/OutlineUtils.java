package org.streeto.osmdata;

import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import javax.json.JsonArray;
import java.util.Arrays;
import java.util.stream.Collectors;

class OutlineUtils {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @NotNull
    public static LinearRing getOutlineFromJson(JsonArray polygon) {
        var coords = polygon.stream().map(value -> {
            var point = value.asJsonArray();
            var lat = point.getJsonNumber(1).doubleValue();
            var lon = point.getJsonNumber(0).doubleValue();
            return new Coordinate(lon, lat);
        }).toArray(Coordinate[]::new);
        return getOutlineFromCoords(coords);
    }

    public static LinearRing getOutlineFromPrefs(String string) {
        var points = string.split("&");
        var coords = Arrays.stream(points).map(point -> {
            var lonlat = point.split(",");
            var lat = Double.parseDouble(lonlat[1]);
            var lon = Double.parseDouble(lonlat[0]);
            return new Coordinate(lon, lat);
        }).toArray(Coordinate[]::new);
        return getOutlineFromCoords(coords);
    }

    @NotNull
    private static LinearRing getOutlineFromCoords(Coordinate[] coords) {
        var seq = new CoordinateArraySequence(coords);
        return new LinearRing(seq, GEOMETRY_FACTORY);
    }

    public static Point getPoint(GHPoint location) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(location.getLon(), location.getLat()));
    }

    public static String toPrefsString(MapData mapData) {
        var date = mapData.getDate().toString();
        var lonlats = Arrays.stream(mapData.getOutline().getCoordinates())
                .map(coordinate -> coordinate.x + "," + coordinate.y)
                .collect(Collectors.toList());
        var outlineStr = String.join("&", lonlats);
        return date + "|" + outlineStr;
    }

    public static boolean isPointInsideOutline(GHPoint location, LinearRing outline) {
        var point = new GeometryFactory().createPoint(new Coordinate(location.getLon(), location.getLat()));
        var polygon = GEOMETRY_FACTORY.createPolygon(outline);
        return polygon.covers(point);
    }

    public static GHPoint getCenter(LinearRing outline) {
        var polygon = GEOMETRY_FACTORY.createPolygon(outline);
        var center = polygon.getCentroid();
        return new GHPoint(center.getY(), center.getX());
    }
}
