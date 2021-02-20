package org.streeto.constraints;

import com.graphhopper.GHResponse;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class LastControlNearTheFinishConstraint implements CourseConstraint {
    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        var lastLeg = takeLast(controls, 2);
        return dist(first(lastLeg), last(lastLeg)) <= 500.0;
    }
}
