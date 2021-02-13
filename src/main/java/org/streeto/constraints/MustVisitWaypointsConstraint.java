package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import static org.streeto.DistUtils.dist;

public class MustVisitWaypointsConstraint implements CourseConstraint{
    @Override
    public boolean valid(@NotNull GHResponse routedCourse){
        PointList waypoints = routedCourse.getBest().getWaypoints();
        if (waypoints.isEmpty() )
            return true;
        else {

            return
                StreamEx.of(waypoints.iterator()).allMatch ( wpt -> getsCloseTo(wpt, routedCourse) );
            }
        }

    private boolean getsCloseTo(GHPoint wpt, GHResponse routedCourse) {
        return StreamEx.of( routedCourse.getBest().getPoints().iterator()).anyMatch( pt -> dist(pt, wpt) < 100.0 );
    }
}
