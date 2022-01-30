package org.streeto.scorers;

import org.junit.jupiter.api.Test;
import org.streeto.StreetOPreferences;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeenThisWayBeforeScorerTest implements ScorerTest {

    @Test
    public void testScore() throws IOException {
        var legRoutes = gpxToGhResponses("/dogLeg.gpx");
        var scorer = new BeenThisWayBeforeScorer(new StreetOPreferences());
        var scores = scorer.apply(legRoutes);
        assertEquals(3, scores.size());
        assertEquals(1.0, scores.get(0)); // first leg, not been here before
        assertEquals(1.0, scores.get(1)); // second leg, not been here before this is a dogleg
        assertTrue(scores.get(2) < 1.0); // third leg, this is dogleg, but also been here before on leg 1
    }
}
