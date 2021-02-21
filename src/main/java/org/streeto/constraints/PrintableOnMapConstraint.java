package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import org.jetbrains.annotations.NotNull;
import org.streeto.mapping.MapFitter;
import org.streeto.utils.Envelope;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.iterableAsStream;

public class PrintableOnMapConstraint implements CourseConstraint {
    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        return routeFitsBox(routedCourse.getAll());
    }

    private boolean routeFitsBox(List<ResponsePath> routes) {
        var env = new Envelope();
        routes.forEach(pw -> iterableAsStream(pw.getPoints())
                .forEach(env::expandToInclude));
        return MapFitter.getForEnvelope(env).isPresent();
    }
}
