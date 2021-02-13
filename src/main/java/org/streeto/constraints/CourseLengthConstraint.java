package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;

public class CourseLengthConstraint implements CourseConstraint{

    private final double desiredDistance;

    public CourseLengthConstraint(double desiredDistance) {
        this.desiredDistance = desiredDistance;
    }
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        double allowedLengthDelta = 0.1;
        double maxAllowedDistance = desiredDistance + desiredDistance * allowedLengthDelta;
        return routedCourse.getBest().getDistance() < maxAllowedDistance;
    }


}
