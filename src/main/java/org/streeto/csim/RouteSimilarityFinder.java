package org.streeto.csim;

import com.graphhopper.ResponsePath;
import org.streeto.StreetOPreferences;

public class RouteSimilarityFinder {

    private final StreetOPreferences preferences;

    public RouteSimilarityFinder(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    public SimilarityResult similarity(ResponsePath a, ResponsePath b) {
        //short circuit if the paths have no length
        if (a.getPoints().size() < 3 || b.getPoints().size() < 3) {
            return SimilarityResult.SAME;
        } else {
            var csim = new CSIM(preferences.getCSIMCellSize());
            return csim.calculateFor(a, b);
        }
    }
}
