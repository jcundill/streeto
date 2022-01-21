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

import com.graphhopper.ResponsePath;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.furniture.StreetFurnitureRepository;
import org.streeto.genetic.CourseFinderRunner;
import org.streeto.genetic.ScatterFinderRunner;
import org.streeto.genetic.Sniffer;
import org.streeto.gpx.GpxFacade;
import org.streeto.kml.KmlWriter;
import org.streeto.mapping.*;
import org.streeto.osmdata.MapDataRepository;
import org.streeto.osmdata.PbfFinder;
import org.streeto.osmdata.PbfInfo;
import org.streeto.tsp.BestSubsetOfTsp;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.streeto.utils.CollectionHelpers.*;


public class StreetO {

    private final String osmDir;
    ControlSiteFinder csf;
    private StreetFurnitureRepository furnitureRepository;
    private final PbfFinder pbfFinder = new PbfFinder();
    private MapSplitter splitter;
    private final List<Sniffer> sniffers = new ArrayList<>();
    private CourseImporter courseImporter = new CourseImporter(null);
    private BBox bounds;
    private CourseScorer scorer;
    private StreetOPreferences preferences;
    private final MapDataRepository mapDataRepository;

    public StreetO(String osmDir, StreetOPreferences prefs) {
        this.osmDir = osmDir;
        preferences = prefs;
        mapDataRepository = new MapDataRepository(osmDir);
    }

    public Optional<PbfInfo> getGeoFabrikExtractDetailsFor(GHPoint location) {
        return pbfFinder.findPbfFor(location);
    }

    public Optional<GraphHopperOSM> initialiseGHFor(GHPoint location) {
        Optional<GraphHopperOSM> maybeGh = mapDataRepository.getMapDataFor(location);
        if (maybeGh.isEmpty()) {
            try {
                maybeGh = mapDataRepository.installMapDataFor(location);
            } catch (IOException e) {
                // just return empty
            }
        }
        if(maybeGh.isPresent()) {
            //process it before we return it
            var gh = maybeGh.get();
            bounds = gh.getGraphHopperStorage().getBounds();
            csf = new ControlSiteFinder(gh, preferences);
            splitter = new MapSplitter(csf, preferences.getPaperSize(), preferences.getMaxMapScale());
            courseImporter = new CourseImporter(csf);
            scorer = new CourseScorer(preferences, csf);
            furnitureRepository = new StreetFurnitureRepository(csf, osmDir);
        }
        return maybeGh;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: StreetO <property file>");
            System.exit(-1);
        }
        var properties = new Properties();
        try {
            properties.load(new FileInputStream(args[0]));
        } catch (InvalidPropertiesFormatException e) {
            System.out.println("Not a valid properties file " + args[0]);
            System.exit(-1);
        } catch (FileNotFoundException e) {
            System.out.println("File not found " + args[0]);
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("Unable to read properties from " + args[0]);
            System.exit(-1);
        }
        // initialize the engine
        var sniffer = new StreetOSniffer();
        var streeto = new StreetO(properties.getProperty("osmDir", "graphs"), new StreetOPreferences());
        streeto.registerSniffer(sniffer);

        // set up the initial course to analyse
        var initialCourse = streeto.getImporter().buildFromProperties(properties);

        // set up the preferences
        var preferences = new StreetOPreferences();
        preferences.setMaxGenerations(1000);
        preferences.setMaxExecutionTime(180);
        preferences.setMaxLastLegLength(300.0);
        preferences.setMinApproachToFinish(100.0);
        preferences.setSplitForBetterScale(false);
        preferences.setPaperSize(PaperSize.A3);
        preferences.setMapStyle(MapStyle.STREETO);
        preferences.setPrintA3OnA4(false);
        preferences.setMaxMapScale(10000.0);
        streeto.setPreferences(preferences);

        // load graphhopper data for the initial course
        var maybeGH = streeto.initialiseGHFor(initialCourse.getControls().get(0).getLocation());

        if(maybeGH.isEmpty()) {
            System.out.println("Unable to load graphhopper data for " + initialCourse.getControls().get(0).getLocation());
            System.exit(-1);
        }

        //generate a good route based on a set of initial parameters
        var maybeControls = streeto.generateCourse(
                initialCourse.getRequestedDistance(),
                initialCourse.getRequestedNumControls(),
                initialCourse.getControls());

        if (maybeControls.isEmpty()) {
            System.out.println("unable to generate a course");
        } else {
            var controls = maybeControls.get();
            var scoreDetails = streeto.score(controls);
            var route = streeto.routeControls(controls);

            System.out.printf("best score: %f%n", scoreDetails.getOverallScore());
            System.out.printf("distance: %f\n", route.getDistance());

            System.out.println(scoreDetails);

            try {
                var outputFolder = new File("./");
                var title = String.format("abc-%d", System.currentTimeMillis());
                streeto.writeGpx(controls, route, title, outputFolder);
                streeto.writeMap(controls, title, new File(outputFolder, title + ".pdf"));
                streeto.writeMapRunFiles(controls, title, outputFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreferences(StreetOPreferences preferences) {
        this.preferences = preferences;
    }

    public void writeMapRunFiles(List<ControlSite> controls, String title, File path) throws IOException {
        var kmlWriter = new KmlWriter();
        var kml = kmlWriter.generate(controls, title);
        var f = new File(path, title + ".kml");
        var fw = new FileWriter(f);
        fw.write(kml);
        fw.flush();
        fw.close();

        var printer = new MapPrinter(preferences);
        var envelopeToMap = csf.getEnvelopeForProbableRoutes(controls);
        printer.generateMapAsKmz(envelopeToMap, title, new File(path, title + ".kmz"));
    }

    public void writeGpx(List<ControlSite> controlSites, ResponsePath route, String title, File outputFolder) throws IOException {
        GpxFacade.writeCourse(new File(outputFolder, title + ".gpx"), route, controlSites);
    }

    public Optional<List<ControlSite>> generateCourse(double distance, int numControls, List<ControlSite> initialControls) {
        if (csf.furniture == null) {
            findFurniture(first(initialControls).getLocation());
        }
        var lastMondayRunner = new CourseFinderRunner(scorer::scoreLegs, csf, sniffers, preferences);
        var maybeBest = lastMondayRunner.run(distance, numControls, initialControls);
        return maybeBest.map(this::renumber);
    }

    public Optional<List<ControlSite>> generateScatterCourse(double distance, int totalControls, int numControls, int iterations, List<ControlSite> initialControls) {
        if (csf.furniture == null) {
            findFurniture(first(initialControls).getLocation());
        }
        var scatterCourseRunner = new ScatterFinderRunner(csf, sniffers, preferences);
        var maybeBest = scatterCourseRunner.run(distance, totalControls, numControls, iterations, initialControls);
        return maybeBest.map(this::renumber);
    }

    private List<ControlSite> renumber(List<ControlSite> best) {
        var renumbered = best.stream().map(cs -> new ControlSite(cs.getLocation(), cs.getDescription())).toList();
        formatNumber(first(renumbered), "S1");
        formatNumber(last(renumbered), "F1");
        forEachIndexed(dropFirstAndLast(renumbered, 1), (i, ctrl) -> formatNumber(ctrl, String.format("%d", i + 1)));
        return renumbered;
    }

    public ScoreDetails score(List<ControlSite> controls) {
        if (csf.furniture == null) {
            findFurniture(first(controls).getLocation());
        }
        return scorer.score(controls);
    }

    public Optional<List<Integer>> runVRP(List<ControlSite> controls, int capacity, int iterations) {
        var solver = new BestSubsetOfTsp(csf);
        var best = solver.solve(controls, capacity, iterations);
        if( best.isPresent() ) {
            var vehicleRoute = best.get();
            var route = vehicleRoute.getTourActivities().getJobs();
            return Optional.of(route.stream().map(Job::getIndex).collect(toList()));
         } else {
            return Optional.empty();
        }
    }

    public void findFurniture(GHPoint start) {
        furnitureRepository.loadForLocation(start);
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

    public ResponsePath routeControls(List<ControlSite> route) {
        return csf.routeRequest(route).getBest();
    }

    public List<ResponsePath> getLegRoutes(ControlSite a, ControlSite b) {
        return csf.findRoutes(a.getLocation(), b.getLocation()).getAll();
    }

    public MapBox getMapBoxFor(List<ControlSite> controls, PaperSize paperSize) {
        var envelopeToMap = csf.getEnvelopeForProbableRoutes(controls);
        return MapFitter.getForEnvelope(envelopeToMap, paperSize, MapFitter.landscape20000.getScale()).orElse(null);
    }

    public void writeMap(List<ControlSite> controls, String mapTitle, File file) throws IOException {

        var printer = new MapPrinter(preferences);
        var envelopeToMap = csf.getEnvelopeForProbableRoutes(controls);
        var mapBox = MapFitter.getForEnvelope(envelopeToMap, preferences.getPaperSize(), preferences.getMaxMapScale()).orElseThrow();

        if (preferences.isSplitForBetterScale()) {
            var maybeSplit = splitter.makeDoubleSidedIfPossible(controls, mapBox);
            if (maybeSplit.isPresent()) {
                printer.generateMapAsPdf(maybeSplit.get(), mapTitle, controls, file);
            } else {
                printer.generateMapAsPdf(envelopeToMap, mapTitle, controls, file);
            }
        } else {
            printer.generateMapAsPdf(envelopeToMap, mapTitle, controls, file);
        }
    }

    public Optional<ControlSite> findNearestControlSiteTo(double lat, double lon) {
        var point = new GHPoint(lat, lon);
        if (csf.furniture == null) {
            findFurniture(new ControlSite(point, "").getLocation());
        }
        return csf.findNearestControlSiteTo(point);
    }

    public BBox getBounds() {
        return bounds;
    }
}