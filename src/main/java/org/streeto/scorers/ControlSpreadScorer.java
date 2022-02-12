package org.streeto.scorers;

import org.streeto.ControlSite;
import org.streeto.StreetOPreferences;

import java.util.List;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;
import static org.streeto.utils.CollectionHelpers.first;

public class ControlSpreadScorer extends ControlSetScorer{
    public ControlSpreadScorer(StreetOPreferences preferences) {
        super(preferences);
    }

    @Override
    public double score(List<ControlSite> controls) {
        var centre = first(controls).getLocation();
        var numberOfControls = dropFirstAndLast(controls, 1);
        var topLeft = numberOfControls.stream().filter(c -> c.getLocation().lat >= centre.lat && c.getLocation().lon < centre.lon).count();
        var topRight = numberOfControls.stream().filter(c -> c.getLocation().lat >= centre.lat && c.getLocation().lon >= centre.lon).count();
        var bottomLeft = numberOfControls.stream().filter(c -> c.getLocation().lat < centre.lat && c.getLocation().lon < centre.lon).count();
        var bottomRight = numberOfControls.stream().filter(c -> c.getLocation().lat < centre.lat && c.getLocation().lon >= centre.lon).count();
        var mean = (topLeft + topRight + bottomLeft + bottomRight) / 4.0;
        var stdDev = Math.sqrt(Math.pow(topLeft - mean, 2) + Math.pow(topRight - mean, 2) + Math.pow(bottomLeft - mean, 2) + Math.pow(bottomRight - mean, 2)) / 4.0;
        var result = 1.0 - stdDev / mean;
        return result;
    }
}
