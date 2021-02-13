/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.streeto;

import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Envelope;
import one.util.streamex.StreamEx;
import org.streeto.furniture.StreetFurnitureFinder;
import org.streeto.genetic.CourseFinderRunner;
import org.streeto.genetic.Sniffer;
import org.streeto.scorers.*;

import java.util.List;


public class Osmosys {

    final ControlSiteFinder csf;
    private final CourseScorer scorer;
    private final StreetFurnitureFinder finder = new StreetFurnitureFinder();

    public Osmosys(String db) {
        GraphHopper gh = new GhWrapper().initGH(db);
        List<LegScorer> featureScorers = List.of(
                new LegLengthScorer(),
                new LegRouteChoiceScorer(),
                new LegComplexityScorer(),
                new BeenThisWayBeforeScorer(),
                new ComingBackHereLaterScorer(),
                new DogLegScorer()
        );
        csf = new ControlSiteFinder(gh);
        scorer = new CourseScorer(featureScorers, csf::findRoutes);
    }

    public ControlSiteFinder getCsf() {
        return csf;
    }

    PathWrapper findBestRoute(List<ControlSite> controls) {
        return csf.routeRequest(controls).getBest();
    }


    Envelope getEnvelopeForProbableRoutes(List<ControlSite> controls) {
        var routes = StreamEx.ofSubLists(controls, 2, 1).map(it ->
                csf.routeRequest(it, 3).getBest()
        ).toList();

        var env = new Envelope();
        routes.forEach(it -> it.getPoints().forEach(p -> env.expandToInclude(p.lon, p.lat)));
        return env;
    }

    Course score(Course course) {
        var route = csf.routeRequest(course.getControls());
        course.setRoute(route.getBest());
        var score = scorer.score(course);
        course.setEnergy(score);
        return course;

    }

    void findFurniture(ControlSite start) {
        var scaleFactor = 5000.0;
        var max = csf.getCoords(start.getPosition(), Math.PI * 0.25, scaleFactor);
        var min = csf.getCoords(start.getPosition(), Math.PI * 1.25, scaleFactor);
        var bbox = new BBox(min.lon, max.lon, min.lat, max.lat);
        csf.setFurniture(finder.findForBoundingBox(bbox));
    }

    public static void main(String [] args) {

        System.out.println("Hello World!");
        var osmosys = new Osmosys("derbyshire-latest");
        var initialCourse = Course.buildFromProperties("./streeto.properties");
        osmosys.findFurniture(initialCourse.getControls().get(0));
        var lastMondayRunner = new CourseFinderRunner(osmosys.csf, new Sniffer());
        var controls = lastMondayRunner.run(initialCourse);
        //var course = courses.first()
        //var course = courses.first()
        var scoredCourse =
                osmosys.score(new Course(initialCourse.distance(), initialCourse.getRequestedNumControls(), controls.getControls()));
        System.out.printf("best score: %f%n", 1.0 - scoredCourse.getEnergy());
        System.out.printf("distance: %f\n", scoredCourse.getRoute().getDistance());

    }
}