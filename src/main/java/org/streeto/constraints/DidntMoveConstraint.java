package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.util.shapes.GHPoint3D;
import org.streeto.StreetOPreferences;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.*;
import static org.streeto.utils.DistUtils.dist;

public class DidntMoveConstraint implements CourseConstraint {
     private final double minLegLength;

    public DidntMoveConstraint(StreetOPreferences preferences) {

        this.minLegLength = preferences.getMinLegLength();
    }

    @Override
    public boolean valid(GHResponse routedCourse) {
        var controls = routedCourse.getBest().getWaypoints();
        return windowed(controls, 2).allMatch(this::evaluate);
    }

    private boolean evaluate(List<GHPoint3D> leg) {
        return dist(first(leg), last(leg)) > minLegLength;
    }
}
