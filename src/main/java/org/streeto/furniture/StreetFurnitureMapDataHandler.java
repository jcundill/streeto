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

package org.streeto.furniture;

import com.graphhopper.util.shapes.GHPoint;
import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.overpass.MapDataWithGeometryHandler;
import org.jetbrains.annotations.NotNull;
import org.streeto.ControlSite;
import org.streeto.ControlType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StreetFurnitureMapDataHandler implements MapDataWithGeometryHandler {

    private final ArrayList<ControlSite> locations;

    public StreetFurnitureMapDataHandler(ArrayList<ControlSite> locations) {
        this.locations = locations;
    }

    private ControlSite parsePointFeature(Node node) {
        return toControlSite(node.getPosition().getLatitude(), node.getPosition().getLongitude(), getDescription(node).replace("_", " "));
    }

    @NotNull
    private ControlSite toControlSite(double lat, double lon, String description) {
        return new ControlSite(new GHPoint(lat, lon), description, ControlType.FURNITURE);
    }

    private String getDescription(Node node) {
        var tags = node.getTags();

        if (tags.containsKey("tourism")) return tags.get("tourism");
        else if (tags.containsKey("highway")) return tags.get("highway");
        else if (tags.containsKey("amenity")) return tags.get("amenity");
        else if (tags.containsKey("natural")) return tags.get("natural");
        else if (tags.containsKey("historic")) return tags.get("historic");
        else return tags.getOrDefault("barrier", "");
    }

    private ControlSite parseLinearFeature(Way way, LatLon loc) {
        return toControlSite(loc.getLatitude(), loc.getLongitude(), getWayDescription(way));
    }

    private String getWayDescription(Way way) {
        var tags = way.getTags();
        if (tags.containsKey("highway") && tags.get("highway").equals("steps")) return "steps";
        else if (tags.containsKey("bridge") && tags.get("bridge").equals("yes")) return "bridge";
        else if (tags.containsKey("barrier") && tags.get("barrier").equals("hedge")) return "hedge";
        else return "";
    }

    @Override
    public void handle(@NotNull BoundingBox bounds) {
    }

    @Override
    public void handle(Node node) {
        if (!node.isDeleted()) {
            locations.add(parsePointFeature(node));
        }
    }

    @Override
    public void handle(@NotNull Way way, @NotNull BoundingBox bounds, List<LatLon> geometry) {
        locations.add(parseLinearFeature(way, geometry.get(0)));
        locations.add(parseLinearFeature(way, geometry.get(geometry.size() - 1)));
    }

    @Override
    public void handle(@NotNull Relation relation, @NotNull BoundingBox boundingBox, @NotNull Map<Long, LatLon> map, @NotNull Map<Long, List<LatLon>> map1) {
        // we only asked about nodes and ways
    }
}

