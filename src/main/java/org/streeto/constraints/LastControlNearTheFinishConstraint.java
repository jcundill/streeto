package org.streeto.constraints;

import com.graphhopper.GHResponse;

import static org.streeto.utils.DistUtils.dist;

public class LastControlNearTheFinishConstraint implements CourseConstraint{
    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        var num = controls.size() - 1;
        return dist(controls.get(num), controls.get(num - 1))  <= 500.0;
    }
}
