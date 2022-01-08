package org.streeto.csim;

import com.graphhopper.ResponsePath;
import org.streeto.StreetOPreferences;

public class RouteSimilarity {

    private final StreetOPreferences preferences;
    public RouteSimilarity(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    public double similarity(ResponsePath a, ResponsePath b) {
        //short circuit if the paths have no length
        if (a.getPoints().size() < 3 || b.getPoints().size() < 3) {
            return 1.0;
        } else {
            var csim = new CSIM(preferences.getCSIMCellSize());
            return csim.calculateFor(a, b);
        }
    }
}
