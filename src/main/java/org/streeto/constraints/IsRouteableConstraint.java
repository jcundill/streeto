package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;

public class IsRouteableConstraint implements CourseConstraint {
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        return !routedCourse.hasErrors();
    }
}
