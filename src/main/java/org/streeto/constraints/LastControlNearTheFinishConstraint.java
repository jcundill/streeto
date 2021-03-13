package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.streeto.StreetOPreferences;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class LastControlNearTheFinishConstraint implements CourseConstraint {
    private final double maxLastLegLength;

    public LastControlNearTheFinishConstraint(StreetOPreferences preferences) {

        this.maxLastLegLength = preferences.getMaxLastLegLength();
    }

    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        var lastLeg = takeLast(controls, 2);
        return dist(first(lastLeg), last(lastLeg)) <= maxLastLegLength;
    }
}
