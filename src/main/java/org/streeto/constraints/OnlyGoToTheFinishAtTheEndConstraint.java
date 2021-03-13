package org.streeto.constraints;

import com.graphhopper.GHResponse;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;


public class OnlyGoToTheFinishAtTheEndConstraint implements CourseConstraint {
    private final double minApproachToFinish;

    public OnlyGoToTheFinishAtTheEndConstraint(StreetOPreferences preferences) {
        this.minApproachToFinish = preferences.getMinApproachToFinish();
    }

    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        var track = routedCourse.getBest().getPoints();
        var finish = last(track);
        return iterableAsStream(track)
                .dropWhile(it -> dist(it, finish) <= minApproachToFinish)
                .dropWhile(it -> dist(it, finish) > minApproachToFinish)
                .allMatch(it -> dist(it, finish) < minApproachToFinish);
    }
}