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
import org.streeto.furniture.StreetFurnitureFinder;
import org.streeto.genetic.CourseFinderRunner;
import org.streeto.genetic.Sniffer;
import org.streeto.gpx.GpxFacade;
import org.streeto.kml.KmlWriter;
import org.streeto.mapping.MapFitter;
import org.streeto.mapping.MapPrinter;
import org.streeto.mapping.MapSplitter;
import org.streeto.scorers.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.streeto.utils.CollectionHelpers.*;


public class StreetO {

    final ControlSiteFinder csf;
    private final CourseScorer scorer;
    private final StreetFurnitureFinder finder = new StreetFurnitureFinder();
    private final MapSplitter splitter;
    private final List<Sniffer> sniffers = new ArrayList<>();
    private final CourseImporter courseImporter;

    public StreetO(String pbf, String osmDir) {
         GraphHopper gh = new GhWrapper().initGH(pbf, osmDir);

        List<LegScorer> featureScorers = List.of(
                new LegLengthScorer(),
                new LegRouteChoiceScorer(),
                new LegComplexityScorer(),
                new BeenThisWayBeforeScorer(),
                new TooCloseToAFutureControlScorer(),
                new DogLegScorer()
        );
        csf = new ControlSiteFinder(gh);
        scorer = new CourseScorer(featureScorers, csf::findRoutes);
        splitter = new MapSplitter(csf);
        courseImporter = new CourseImporter(csf);
    }


    public void writeMap(List<ControlSite> controls, String mapTitle, File path) throws IOException {

        File file = new File(path.getAbsoluteFile(), mapTitle + ".pdf");
        var printer = new MapPrinter();
        var envelopeToMap = csf.getEnvelopeForProbableRoutes(controls);
        var mapBox = MapFitter.getForEnvelope(envelopeToMap).orElseThrow();

        var splitResult = splitter.makeDoubleSidedIfPossible(controls, mapBox);
        if( splitResult.isEmpty()) {
            System.out.println("Not splitting");
             printer.generateMapAsPdf( envelopeToMap, mapTitle, controls, file);
            //printer.generateMapAsKmz("abc.kmz", "Test_Map");
        } else {
            System.out.println("Splitting");
            printer.generateMapAsPdf(splitResult.get(), mapTitle, controls, file);
        }
    }

    public void writeMapRunFiles(List<ControlSite> controls, String title, File path) throws IOException {
        var kmlWriter = new KmlWriter();
        var kml = kmlWriter.generate(controls, title);
        var f = new File( path, title + ".kml");
        var fw = new FileWriter(f);
        fw.write(kml);
        fw.flush();
        fw.close();

        var printer = new MapPrinter();
        var envelopeToMap = csf.getEnvelopeForProbableRoutes(controls);
        printer.generateMapAsKmz(envelopeToMap, title, new File(path,title + ".kmz"));
    }

    public void writeGpx(List<ControlSite> controlSites, PathWrapper route, String title, File outputFolder) throws IOException {
        GpxFacade.writeCourse(new File(outputFolder, title + ".gpx"), route, controlSites);
    }

    public Optional<List<ControlSite>> generateCourse(double distance, int numControls, List<ControlSite> initialControls) {
        findFurniture(initialControls.get(0));
        var lastMondayRunner = new CourseFinderRunner(scorer::scoreLegs, csf, sniffers);
        var maybeBest = lastMondayRunner.run(distance, numControls, initialControls);
        return maybeBest.map( best -> {
            formatNumber(first(best), "S1");
            forEachIndexed(dropFirstAndLast(best, 1), (i, ctrl) -> formatNumber(ctrl, String.format("%d", i + 1)));
            formatNumber(last(best), "F1");
            return best;
        });
     }

    public ScoreDetails score(List<ControlSite> controls) {
        return scorer.score(controls);
    }

     private void findFurniture(ControlSite start) {
        var scaleFactor = 5000.0;
        var max = csf.getGHPointRelativeTo(start.getLocation(), Math.PI * 0.25, scaleFactor);
        var min = csf.getGHPointRelativeTo(start.getLocation(), Math.PI * 1.25, scaleFactor);
        var bbox = new BBox(min.lon, max.lon, min.lat, max.lat);
        csf.setFurniture(finder.findForBoundingBox(bbox));
    }


    private void formatNumber(ControlSite controlSite, String format) {
        controlSite.setNumber(format);
    }

    public void registerSniffer(Sniffer sniffer) {
        sniffers.add(sniffer);
    }

    public void unregisterSniffer(Sniffer sniffer) {
        sniffers.remove(sniffer);
    }

    public CourseImporter getImporter() {
        return courseImporter;
    }

    public PathWrapper routeControls(List<ControlSite> route) {
        return csf.routeRequest(route).getBest();
    }

    public List<PathWrapper> getLegRoutes(ControlSite a, ControlSite b) {
        return csf.findRoutes(a.getLocation(), b.getLocation()).getAll();
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");

        // initializr the engine
        var sniffer = new Sniffer();
        var streeto = new StreetO("extracts/great-britain-latest.osm.pbf", "osm_data");
        streeto.registerSniffer(sniffer);

        // set up the initial course to analyse
        var initialCourse = streeto.getImporter().buildFromProperties("./streeto.properties");

        //generate a good route based on a set of initial parameters
        var maybeControls = streeto.generateCourse(
                initialCourse.getRequestedDistance(),
                initialCourse.getRequestedNumControls(),
                initialCourse.getControls());

        if( maybeControls.isEmpty()) {
            System.out.println("unable to generate a course");
        } else {
            var controls = maybeControls.get();
            var scoreDetails = streeto.score(controls);
            var route = streeto.routeControls(controls);

            System.out.printf("best score: %f%n", scoreDetails.getOverallScore());
            System.out.printf("distance: %f\n", route.getDistance());

            System.out.println(scoreDetails.toString());

            try {
                var outputFolder = new File("./");
                streeto.writeGpx(controls, route,"abc", outputFolder);
                streeto.writeMap(controls,"abc", outputFolder);
                streeto.writeMapRunFiles(controls, "abc", outputFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}