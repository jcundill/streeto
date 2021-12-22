package org.streeto.furniture;

import com.graphhopper.util.shapes.BBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StreetFurnitureFinderTest {
    @Test
    void testFindForBoundingBox() {
        StreetFurnitureFinder streetFurnitureFinder = new StreetFurnitureFinder();
        BBox bbox = new BBox(0, 0, 0, 0);
        assertTrue(streetFurnitureFinder.findForBoundingBox(bbox).isEmpty());
    }
}

