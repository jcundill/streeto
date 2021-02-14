package org.streeto.constraints;

import com.graphhopper.GHResponse;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.streeto.utils.CollectionHelpers;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;


public class OnlyGoToTheFinishAtTheEndConstraint implements CourseConstraint {
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        var track = routedCourse.getBest().getPoints();
        var finish = track.get(track.getSize() - 1);
        return streamFromPointList(track)
                .dropWhile(it1 -> dist(it1, finish) <= 150.0)
                .dropWhile(it1 -> dist(it1, finish) > 150.0)
                .allMatch(it -> dist(it, finish) < 150.0);
    }
}