package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;

public class IsRouteableConstraint implements CourseConstraint {
    public boolean test(@NotNull GHResponse routedCourse) {
        return !routedCourse.hasErrors();
    }
}
