package org.streeto.scorers;

import org.junit.jupiter.api.Test;
import org.streeto.StreetOPreferences;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DogLegScorerTest implements ScorerTest {

    @Test
    public void test() throws IOException {
        var routedLegs = gpxToGhResponses("/dogLeg.gpx");

        var scorer = new DogLegScorer(new StreetOPreferences());

        var scores = scorer.apply(routedLegs);
        assertEquals(3, scores.size());
        assertEquals(1.0, scores.get(0));
        assertTrue(scores.get(1) < 1.0);
        assertTrue( scores.get(2) < 1.0);
    }
}
