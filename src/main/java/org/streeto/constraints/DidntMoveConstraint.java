package org.streeto.constraints;

import com.graphhopper.GHResponse;

import static org.streeto.DistUtils.dist;

public class DidntMoveConstraint implements CourseConstraint{
    private final double minMoveDistance = 50.0;

    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        return CollectionHelpers.windowed(controls,2).allMatch(it -> dist(it.get(0), it.get(1)) > minMoveDistance);
    }
}
