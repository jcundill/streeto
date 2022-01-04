package org.streeto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreDetailsTest {
    @Test
    void testGetOverallScore() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("feature1", List.of(0.5, 0.5));
        var legScores = List.of(0.5, 0.5);
        assertEquals(0.25, (new ScoreDetails(legScores, featureScores)).getOverallScore());
    }

    @Test
    void testGetOverallScore2() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("feature1", List.of(0.5, 0.0));
        var legScores = List.of(0.5, 0.0);
        assertEquals(0.125, (new ScoreDetails(legScores, featureScores)).getOverallScore());
    }

    @Test
    void testGetLegDetails() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("a", List.of(0.5, 0.0));
        featureScores.put("b", List.of(1.0, 1.0));
        var legScores = List.of(0.75, 0.5);
        var details = new ScoreDetails(legScores, featureScores);
        var expected = List.of(List.of(0.5, 1.0), List.of(0.0, 1.0));
        assertEquals(expected, details.getLegDetails());
    }
}

