package org.streeto.furniture;

import com.graphhopper.util.shapes.BBox;
import org.junit.jupiter.api.Test;
import org.streeto.ControlSite;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StreetFurnitureFinderTest {
    @Test
    void testFindForBoundingBox() {
        StreetFurnitureFinder streetFurnitureFinder = new StreetFurnitureFinder();
        BBox bbox = new BBox(0, 0, 0, 0);
        Optional<List<ControlSite>> forBoundingBox = streetFurnitureFinder.findForBoundingBox(bbox);
        assertTrue( forBoundingBox.isPresent() );
        assertTrue(forBoundingBox.get().isEmpty());
    }
}

