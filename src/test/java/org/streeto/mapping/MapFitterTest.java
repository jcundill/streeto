package org.streeto.mapping;

import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;
import org.streeto.utils.Envelope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapFitterTest {
    @Test
    void testCanBeMapped() {
        assertTrue(MapFitter.canBeMapped(new Envelope(), PaperSize.A4, 5000.0));
        assertTrue(MapFitter.canBeMapped(new Envelope(), PaperSize.A3, 5000.0));
    }

    @Test
    void testCannotBeMapped() {
        var envelope = new Envelope();
        envelope.expandToInclude(new GHPoint(53.1, 0.0));
        envelope.expandToInclude(new GHPoint(55.1, 0.0));
        assertFalse(MapFitter.canBeMapped(envelope, PaperSize.A4, 5000.0));
        assertFalse(MapFitter.canBeMapped(envelope, PaperSize.A3, 5000.0));
    }
}

