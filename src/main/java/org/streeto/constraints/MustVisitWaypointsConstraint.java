package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;

import static org.streeto.utils.CollectionHelpers.streamFromPointList;
import static org.streeto.utils.DistUtils.dist;

public class MustVisitWaypointsConstraint implements CourseConstraint {
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        PointList waypoints = routedCourse.getBest().getWaypoints();
        if (waypoints.isEmpty())
            return true;
        else {
            return streamFromPointList(waypoints).allMatch(wpt -> getsCloseTo(wpt, routedCourse));
        }
    }

    private boolean getsCloseTo(GHPoint wpt, GHResponse routedCourse) {
        return streamFromPointList(routedCourse.getBest().getPoints()).anyMatch(pt -> dist(pt, wpt) < 100.0);
    }
}
