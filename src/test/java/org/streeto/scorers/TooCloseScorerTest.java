package org.streeto.scorers;

import com.graphhopper.GHResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.streeto.ControlSite;
import org.streeto.StreetOPreferences;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TooCloseScorerTest implements ScorerTest {


    @BeforeAll
    public static void setUpClass() throws IOException {
    }

    @Test
    public void test() throws IOException {
        String name = "/tooClose.gpx";
        List<GHResponse> routedLegs = gpxToGhResponses(name);
        List<ControlSite> controlSites = gpxToControlSites(name);

        var names = controlSites.stream().map(ControlSite::getNumber).toList();
        assertIterableEquals(List.of("S1", "1", "2", "F1"), names);

        assertEquals(3, routedLegs.size());

        var scorer = new TooCloseToAFutureControlScorer(new StreetOPreferences());
        var scores = scorer.apply(routedLegs);
        assertEquals(3, scores.size());
        assertEquals(0.0, scores.get(0));
        assertEquals(1.0, scores.get(1));
        assertEquals(1.0, scores.get(2));
     }


}
