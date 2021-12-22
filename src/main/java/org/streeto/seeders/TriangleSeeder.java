package org.streeto.seeders;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.List;

public class TriangleSeeder extends AbstractSeeder {

    public TriangleSeeder(ControlSiteFinder csf) {
        super(csf);
    }

    @Override
    public List<ControlSite> seed(List<ControlSite> initialPoints, int requestedNumControls, double requestedCourseLength) {
        var circLength = 3.5;
        var angle = 1.5658238;

        var scaleFactor = requestedCourseLength * twistFactor / circLength;
        var csf = getCsf();
        var first = initialPoints.get(0);
        var last = initialPoints.get(initialPoints.size() - 1);
        var bearing = csf.randomBearing();
        var second = csf.getGHPointRelativeTo(first.getLocation(), Math.PI + bearing, scaleFactor);
        var third = csf.getGHPointRelativeTo(last.getLocation(), Math.PI + bearing + angle, scaleFactor);

        var merged = merge(initialPoints, List.of(second, third), requestedNumControls);
        return generateInitialCourse(merged, requestedNumControls);
    }
}