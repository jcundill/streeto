package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;
import org.streeto.ControlSite;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.iterableAsStream;
import static org.streeto.utils.DistUtils.dist;

public class MustVisitWaypointsConstraint implements CourseConstraint {

    private final List<ControlSite> toVisit;

    public MustVisitWaypointsConstraint(List<ControlSite> toVisit) {
        this.toVisit = toVisit;
    }
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
         if (toVisit.isEmpty())
            return true;
        else {
            return toVisit.stream().allMatch(wpt -> getsCloseTo(wpt, routedCourse));
        }
    }

    private boolean getsCloseTo(ControlSite wpt, GHResponse routedCourse) {
        return iterableAsStream(routedCourse.getBest().getPoints()).anyMatch(pt -> dist(pt, wpt.getLocation()) < 100.0);
    }
}
