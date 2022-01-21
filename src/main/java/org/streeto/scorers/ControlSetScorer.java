package org.streeto.scorers;

import org.streeto.ControlSite;
import org.streeto.StreetOPreferences;

import java.util.List;

abstract public class ControlSetScorer {
    protected StreetOPreferences preferences;

    protected ControlSetScorer(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    abstract public double score(List<ControlSite> controls);
}
