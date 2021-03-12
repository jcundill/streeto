package org.streeto.constraints;

import com.graphhopper.GHResponse;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class FirstControlNearTheStartConstraint implements CourseConstraint {
    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        var firstLeg = take(controls, 2);
        return dist(first(firstLeg), last(firstLeg)) <= 750.0;
    }
}
