package org.streeto.osmdata;

import com.graphhopper.util.shapes.BBox;

public class MapData {

    private final BBox bbox;
    private final String name;

    public MapData(String name, BBox bbox) {
        this.bbox = bbox;
        this.name = name;
    }

    public BBox getBbox() {
        return bbox;
    }

    public String getName() {
        return name;
    }
}
