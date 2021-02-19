package org.streeto.kml;


import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.streeto.utils.CollectionHelpers.first;
import static org.streeto.utils.CollectionHelpers.last;

class KmlWriterTest {

    @Test
    void readFile() {
        var fis = getClass().getClassLoader().getResourceAsStream("testcourse.kml");
        var kml = new KmlWriter();
        assertDoesNotThrow(() -> {
                    var xs = kml.parseStream(fis).collect(Collectors.toList());
                    assertTrue(xs.size() > 0);
                    assertEquals("S1", first(xs).getNumber());
                    assertEquals("F1", last(xs).getNumber());
                }
        );
    }

    @Test
    void generate() throws Exception {
        var fis = getClass().getClassLoader().getResourceAsStream("testcourse.kml");
        var kml = new KmlWriter();
        var xs = kml.parseStream(fis).collect(Collectors.toList());
        var ys = kml.generate(xs, "myid");
    }
}