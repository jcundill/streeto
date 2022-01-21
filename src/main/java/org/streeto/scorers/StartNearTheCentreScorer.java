package org.streeto.scorers;

import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSite;
import org.streeto.StreetOPreferences;
import org.streeto.utils.Envelope;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;
import static org.streeto.utils.DistUtils.dist;

public class StartNearTheCentreScorer extends ControlSetScorer{

    public StartNearTheCentreScorer(StreetOPreferences preferences) {
        super(preferences);
    }

    @Override
    public double score(List<ControlSite> controls) {
        var numberedControls = dropFirstAndLast(controls, 1).stream().map(ControlSite::getLocation).toList();
        var envelope = new Envelope();
        for (GHPoint numberedControl : numberedControls) {
            envelope.expandToInclude(numberedControl);
        }
        var centre = envelope.centre();

        return dist(centre, controls.get(0).getLocation()) > 1000.0 ? 0.0 : 1.0;
    }


}
