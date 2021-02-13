package org.streeto.seeders;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.List;

import static java.lang.Math.PI;

public class CentredFatHourglassSeeder extends AbstractSeeder {


    public CentredFatHourglassSeeder(ControlSiteFinder csf) {
        super(csf);
    }

    @Override
    public List<ControlSite> seed(List<ControlSite> initialPoints, int requestedNumControls, double requestedCourseLength) {
        var longRatio = 1.414;
        var shortRatio = 1.0 / longRatio;
        var angle = 1.570796327;

        var csf = getCsf();

        var scaleFactor = requestedCourseLength * twistFactor / 4.5; //its a rectangle and a bit

        var first = initialPoints.get(0);
        var last = initialPoints.get(initialPoints.size() - 1);
        var bearing = csf.randomBearing();
        var initial = csf.getCoords(first.getPosition(), bearing, scaleFactor * longRatio / 2.0);
        var second = csf.getCoords(initial, bearing + angle, scaleFactor * shortRatio / 2.0);
        var third = csf.getCoords(second, PI + bearing + angle, scaleFactor * shortRatio );
        var fourth =  csf.getCoords(third, PI + bearing, scaleFactor * longRatio);
        var fifth = csf.getCoords(fourth, bearing + angle, scaleFactor * longRatio);

        var points = List.of(first.getPosition(), second, fifth, fourth, third, last.getPosition());
        return generateInitialCourse(points, requestedNumControls);
    }

}