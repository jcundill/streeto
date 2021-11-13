package org.streeto.scorers;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.streeto.StreetOPreferences;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.streeto.utils.CollectionHelpers.*;

class LegLengthScorerTest implements ScorerTestHelpers {

    @Property
    void testLegLengthScoring(
            @ForAll("routed legs") List<GHResponse> routedLegs,
            @ForAll double minLegLen,
            @ForAll double maxLegLen,
            @ForAll double maxFirstLen,
            @ForAll double maxLastLen
    ) {
        var prefs = new StreetOPreferences();
        prefs.setMinLegDistance(minLegLen);
        prefs.setMaxLegDistance(maxLegLen);
        prefs.setMaxLastLegLength(maxLastLen);
        prefs.setMaxFirstControlDistance(maxFirstLen);
        LegLengthScorer legLengthScorer = new LegLengthScorer(prefs);
        var scores = legLengthScorer.apply(routedLegs);
        assertTrue(scores.stream().allMatch(it -> it >= 0.0 && it <= 1.0));
        var firstDist=  first(routedLegs).getBest().getDistance();
        var firstShouldBeOk = firstDist <= prefs.getMaxFirstControlDistance() && firstDist >= prefs.getMinLegDistance();
        assertEquals(firstShouldBeOk, first(scores) == 1.0);
        assertEquals(!firstShouldBeOk, first(scores) == 0.0);

        var lastDist = last(routedLegs).getBest().getDistance();
        var lastShouldBeOk = lastDist <= prefs.getMaxLastLegLength() && lastDist >= prefs.getMinLegDistance();
        assertEquals(lastShouldBeOk, last(scores) == 1.0);
        assertEquals(!lastShouldBeOk, last(scores) == 0.0);

        forEachZipped(dropFirstAndLast(routedLegs, 1), dropFirstAndLast(scores, 1), (leg, score) -> {
            var legDist = leg.getBest().getDistance();
            if( legDist < prefs.getMinLegDistance() ) {
                assertEquals(0.0, score);
            } else if (legDist > prefs.getMaxLegDistance()) {
                assertEquals(0.0, score);
            } else {
                assertEquals(1.0, score);
            }
        });
    }


    @Test
    void testConstructor() {
        assertEquals(1.0, (new LegLengthScorer(new StreetOPreferences())).getWeighting());
    }

    @Test
    void testScore() {
        LegLengthScorer legLengthScorer = new LegLengthScorer(new StreetOPreferences());
        assertTrue(legLengthScorer.apply(new ArrayList<>()).isEmpty());
    }

    @Test
    void testScore2() {
        LegLengthScorer legLengthScorer = new LegLengthScorer(new StreetOPreferences());

        GHResponse ghResponse = new GHResponse();
        ghResponse.add(new ResponsePath());

        ArrayList<GHResponse> ghResponseList = new ArrayList<>();
        ghResponseList.add(ghResponse);
        List<Double> actualScoreResult = legLengthScorer.apply(ghResponseList);
        assertEquals(1, actualScoreResult.size());
        assertEquals(0.0, actualScoreResult.get(0));
    }
}

