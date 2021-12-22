package org.streeto.utils;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvelopeTest {
    @Test
    void testExpandToInclude() {
        Envelope envelope = new Envelope();
        envelope.expandToInclude(new GHPoint(10.0, 15.0));
        envelope.expandToInclude(new GHPoint(20.0, 25.0));
        assertEquals(20.0, envelope.maxLat);
        assertEquals(25.0, envelope.maxLon);
        assertEquals(10.0, envelope.minLat);
        assertEquals(15.0, envelope.minLon);
    }


    @Test
    void testCentre() {
        Envelope envelope = new Envelope();
        envelope.expandToInclude(new GHPoint(10.0, 15.0));
        envelope.expandToInclude(new GHPoint(20.0, 25.0));
        var centre = envelope.centre();
        assertEquals(15.0, centre.getLat());
        assertEquals(20.0, centre.getLon());
    }
}

