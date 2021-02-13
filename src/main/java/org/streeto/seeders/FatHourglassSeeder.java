package org.streeto.seeders;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.List;

public class FatHourglassSeeder extends AbstractSeeder {

    public FatHourglassSeeder(ControlSiteFinder csf) {
        super(csf);
    }

    @Override
    public List<ControlSite> seed(List<ControlSite> initialPoints, int requestedNumControls, double requestedCourseLength) {
        var longRatio = 1.414;
        var shortRatio = 1.0 / longRatio;
        var angle = 1.570796327;

        var scaleFactor = requestedCourseLength * twistFactor / 4.0; //its a rectangle

        var csf = getCsf();
        var first = initialPoints.get(0);
        var last = initialPoints.get(initialPoints.size() - 1);
        var bearing = csf.randomBearing();
        var second = csf.getCoords(first.getPosition(), Math.PI + bearing, scaleFactor * longRatio);
        var third = csf.getCoords(second, Math.PI + bearing + angle, scaleFactor * shortRatio);
        var fourth =  csf.getCoords(third, bearing, scaleFactor * longRatio);

        var points = List.of(first.getPosition(), third, second, fourth, last.getPosition());
        return generateInitialCourse(points, requestedNumControls);
    }

}