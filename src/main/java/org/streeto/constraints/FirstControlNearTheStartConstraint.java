package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.streeto.StreetOPreferences;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class FirstControlNearTheStartConstraint implements CourseConstraint {
    private final double maxFirstControlDistance;

    public FirstControlNearTheStartConstraint(StreetOPreferences preferences) {

        this.maxFirstControlDistance = preferences.getMaxFirstControlDistance();
    }

    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        var firstLeg = take(controls, 2);
        return dist(first(firstLeg), last(firstLeg)) <= maxFirstControlDistance;
    }
}
