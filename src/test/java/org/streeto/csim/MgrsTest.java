package org.streeto.csim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MgrsTest {

    @Test
    public void testConvert() {
        var lat = 52.3456;
        var lon = -1.2345;
        var cell = MGRSConverter.convert(lat, lon);
        var mgrsString = String.format("%s%05d%05d", cell.zone, cell.x, cell.y);

        var point = MGRSConverter.convertToLatLon(mgrsString);
        assertEquals(lat, point.getLatitude(), 0.00001);
        assertEquals(lon, point.getLongitude(), 0.00001);
    }

    @Test
    public void testAustralia() {
        var lat = -37.3452;
        var lon = 143.858780;
        var cell = MGRSConverter.convert(lat, lon);
        var mgrsString = String.format("%s%05d%05d", cell.zone, cell.x, cell.y);

        var point = MGRSConverter.convertToLatLon(mgrsString);
        assertEquals(lat, point.getLatitude(), 0.00001);
        assertEquals(lon, point.getLongitude(), 0.00001);
    }
}
