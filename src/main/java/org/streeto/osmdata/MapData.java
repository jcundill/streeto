package org.streeto.osmdata;

import org.locationtech.jts.geom.LinearRing;

import java.time.LocalDate;

public class MapData {

    private final LinearRing outline;
    private final String name;
    private final LocalDate date;

    public MapData(String name, LinearRing outline, LocalDate date) {
        this.outline = outline;
        this.name = name;
        this.date = date;
    }

    public LinearRing getOutline() {
        return outline;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }
}
