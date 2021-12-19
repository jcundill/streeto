package org.streeto.osmdata;


import org.locationtech.jts.geom.LinearRing;

public class PbfInfo {
    private final String url;
    private final LinearRing outline;

    public PbfInfo(String url, LinearRing outline) {
        this.url = url;
        this.outline = outline;
    }

    public String getUrl() {
        return url;
    }

    public LinearRing getOutline() {
        return outline;
    }
}
