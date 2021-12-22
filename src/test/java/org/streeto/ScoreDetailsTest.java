package org.streeto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreDetailsTest {
    @Test
    void testGetOverallScore() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("feature1", List.of(10.0, 10.0));
        var legScores = List.of(10.0, 10.0);
        assertEquals(10.0, (new ScoreDetails(legScores, featureScores)).getOverallScore());
    }

    @Test
    void testGetOverallScore2() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("feature1", List.of(10.0, 0.0));
        var legScores = List.of(10.0, 0.0);
        assertEquals(5.0, (new ScoreDetails(legScores, featureScores)).getOverallScore());
    }

    @Test
    void testGetOverallScore3() {
        var featureScores = new HashMap<String, List<Double>>();
        featureScores.put("a", List.of(10.0, 0.0));
        featureScores.put("b", List.of(20.0, 30.0));
        var legScores = List.of(15.0, 15.0);
        var details = new ScoreDetails(legScores, featureScores);
        var expected = List.of(List.of(10.0, 20.0), List.of(0.0, 30.0));
        assertEquals(expected, details.getLegDetails());
    }
}

