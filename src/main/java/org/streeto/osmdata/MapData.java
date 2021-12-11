package org.streeto.osmdata;

import com.graphhopper.util.shapes.BBox;

public class MapData {

    private final BBox bbox;
    private final String path;

    public MapData(String path, BBox bbox) {
        this.bbox = bbox;
        this.path = path;
    }

    public BBox getBbox() {
        return bbox;
    }

    public String getPath() {
        return path;
    }
}
