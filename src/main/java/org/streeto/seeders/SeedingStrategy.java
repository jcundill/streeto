package org.streeto.seeders;

import org.streeto.ControlSite;
import java.util.List;

public interface SeedingStrategy {
    List<ControlSite> seed( List<ControlSite> initialPoints, int requestedNumControls, double requestedCourseLength);
}