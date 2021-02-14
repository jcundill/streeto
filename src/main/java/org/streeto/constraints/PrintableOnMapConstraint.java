package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.vividsolutions.jts.geom.Envelope;
import org.jetbrains.annotations.NotNull;
import org.streeto.mapping.MapFitter;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.streamFromPointList;

public class PrintableOnMapConstraint implements CourseConstraint{
    private final Envelope env = new Envelope();

    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        return routeFitsBox(routedCourse.getAll());
    }

    private boolean routeFitsBox(List<PathWrapper> routes) {
        env.setToNull();
        routes.forEach( pw -> streamFromPointList( pw.getPoints()).forEach (it -> env.expandToInclude(it.lon, it.lat) ) );
        return MapFitter.getForEnvelope(env).isPresent();
    }
}
