package org.streeto;

import com.graphhopper.GHResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseScorerTest {

    @Test
    void transpose() {
        var a = List.of(
                List.of(1,2,3,4),
                List.of(1,2,3,4),
                List.of(1,2,3,4)
        );
        var expected = List.of(
                List.of(1,1,1),
                List.of(2,2,2),
                List.of(3,3,3),
                List.of(4,4,4)
        );
        var scorer = new CourseScorer(List.of(), (x,b) -> new GHResponse());
        assertEquals(expected, scorer.transpose(a));
    }
}