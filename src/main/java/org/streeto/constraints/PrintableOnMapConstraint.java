package org.streeto.constraints;

import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import org.jetbrains.annotations.NotNull;
import org.streeto.StreetOPreferences;
import org.streeto.mapping.MapBox;
import org.streeto.mapping.MapFitter;
import org.streeto.mapping.PaperSize;
import org.streeto.utils.Envelope;

import java.util.List;
import java.util.Optional;

import static org.streeto.utils.CollectionHelpers.iterableAsStream;

public class PrintableOnMapConstraint implements CourseConstraint {
    private final double maxMapScale;
    private final PaperSize paperSize;


    public PrintableOnMapConstraint(StreetOPreferences preferences) {
        this.maxMapScale = preferences.getMaxMapScale();
        this.paperSize = preferences.getPaperSize();
    }

    @Override
    public boolean valid(@NotNull GHResponse routedCourse) {
        return routeFitsBox(routedCourse.getAll());
    }

    private boolean routeFitsBox(List<ResponsePath> routes) {
        var env = new Envelope();
        routes.forEach(pw -> iterableAsStream(pw.getPoints())
                .forEach(env::expandToInclude));
        Optional<MapBox> maybeMap = MapFitter.getForEnvelope(env, paperSize, maxMapScale);
        return maybeMap.isPresent();
    }
}
