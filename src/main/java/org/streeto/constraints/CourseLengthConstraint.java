package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

public class CourseLengthConstraint implements CourseConstraint {

    private final double desiredDistance;
    private final double allowedLengthDelta;

    public CourseLengthConstraint(double desiredDistance, StreetOPreferences preferences) {
        this.desiredDistance = desiredDistance;
        this.allowedLengthDelta = preferences.getAllowedCourseLengthDelta();
    }

    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        double maxAllowedDistance = desiredDistance + desiredDistance * allowedLengthDelta;
        return routedCourse.getBest().getDistance() < maxAllowedDistance;
    }
}
