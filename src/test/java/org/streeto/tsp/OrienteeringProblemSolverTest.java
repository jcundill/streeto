package org.streeto.tsp;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.util.shapes.GHPoint;
import org.apache.pdfbox.contentstream.operator.state.Concatenate;
import org.junit.jupiter.api.Test;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.StreetOPreferences;
import org.streeto.osmdata.GhWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrienteeringProblemSolverTest {

    double[][] controls = new double[][]{
            {17, 26}, //start
            {4, 22}, //1
            {15, 16},// 2
            {27, 10}, // 3
            {22, 22}, // 4
            {33, 28}, // 5
            {17, 34}, // 6
            {7, 41}, // 7
            {23, 39}, // 8
            {14, 46} //  9
    };
    int[] values = new int[]{0, 50, 30, 50, 10, 50, 10, 20, 20, 50};

    double[][] bigControls = new double[][]{
            {17, 26}, //start
            {4, 22}, //1
            {15, 16},// 2
            {27, 10}, // 3
            {22, 22}, // 4
            {33, 28}, // 5
            {17, 34}, // 6
            {7, 41}, // 7
            {23, 39}, // 8
            {14, 46}, //  9
            {36, 28}, //1
            {25, 34},// 2
            {13, 40}, // 3
            {18, 28}, // 4
            {7, 22}, // 5
            {23, 16}, // 6
            {33, 9}, // 7
            {17, 11}, // 8
            {26, 4} //  9

    };

    int[] bigValues = new int[]{0, 50, 30, 50, 10, 50, 10, 20, 20, 50, 50, 30, 50, 10, 50, 10, 20, 20, 50};


    @Test
    public void testTwoOpt() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var route = ts.twoOpt(List.of(0, 5, 8, 4, 6, 0));
        System.out.println("route = " + route);
        assertEquals(List.of(0, 6, 8, 5, 4, 0), route);
    }

    @Test
    public void testTwoOpt2() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var route = ts.twoOpt(List.of(0, 6, 8, 5, 4, 0));
        System.out.println("route = " + route);
        assertEquals(List.of(0, 6, 8, 5, 4, 0), route);
    }

    @Test
    public void testTwoOpt3() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var route = ts.twoOpt(List.of(0, 6, 8, 5, 4, 2, 3, 1, 9, 0));
        System.out.println("route = " + route);
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 8, 9, 6, 0), route);
    }

    @Test
    public void testThreeOpt() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var route = ts.threeOpt(List.of(0, 3, 6, 2, 4, 1, 0));
        System.out.println("route = " + route);
        // assertTrue(route.size() == 2);
    }

    @Test
    public void testInit() {
        var ts = new OrienteeringProblemSolver(controls, values, 185.0);
        var startingRoute = ts.initialization(controls.length);
        System.out.println("startingRoute = " + startingRoute);
        System.out.println("ts.calculateTour(startingRoute) = " + ts.calculateTour(startingRoute));
        assertTrue(ts.calculateTour(startingRoute) < ts.calculateTour(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0)));
        assertTrue(abs(controls.length / 2 - startingRoute.size()) <= 1);
        // assertTrue(startingRoute.size() == 2);
    }

    @Test
    public void testBigInit() {
        var ts = new OrienteeringProblemSolver(bigControls, bigValues, 285.0);
        var startingRoute = ts.initialization(bigControls.length);
        System.out.println("startingRoute = " + startingRoute);
        System.out.println("ts.calculateTour(startingRoute) = " + ts.calculateTour(startingRoute));
        assertTrue(ts.calculateTour(startingRoute) < ts.calculateTour(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0)));
        assertTrue(abs(bigControls.length / 2 - startingRoute.size()) <= 1);
        // assertTrue(startingRoute.size() == 2);
    }

    @Test
    public void testDispersionIndex() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var index1 = ts.dispersionIndex(List.of(0, 1, 3, 5, 0));
        var index2 = ts.dispersionIndex(List.of(0, 1, 3, 9, 0));
        assertTrue(index2 > index1);
    }

    @Test
    public void testRThing() {
        var N = 52;//controls.length;
        var r = List.of(1, (int) (N / 2.0), (int) (2 * N / 3.0), (int) (3 * N / 4.0), (int) (4 * N / 5.0), (int) (5 * N / 6.0), (int) (6 * N / 7.0), (int) (7 * N / 8.0), (int) (8 * N / 9.0), (int) (9 * N / 10.0));
        System.out.println("r = " + r);
    }

    @Test
    public void testProximityMeasure() {
        var ts = new OrienteeringProblemSolver(controls, values, 600);
        var index = ts.proximityMeasure(List.of(4, 3, 2), List.of(6, 8, 9));
        System.out.println("index = " + index);
        var index2 = ts.proximityMeasure(List.of(4, 3, 2), List.of(1, 2, 3));
        System.out.println("index = " + index2);
    }

    @Test
    public void testInsertionCandidates() {
        var ts = new OrienteeringProblemSolver(controls, values, 60);
        var candidates = ts.insertionCandidates(controls.length);
        candidates.forEach(System.out::println);
    }

    @Test
    public void testDeletionCandidates() {
        var ts = new OrienteeringProblemSolver(controls, values, 300.0);
        var candidates = ts.deletionCandidates(List.of(0, 2, 3, 4, 9, 0));
        candidates.forEach(System.out::println);
        assertTrue(candidates.size() > 0);
        assertTrue(candidates.contains(List.of(9)));
    }

    @Test
    public void testFindBestInsertionCandidate() {
        var ts = new OrienteeringProblemSolver(controls, values, 600);
        List<Integer> route = List.of(0, 6, 7, 8);
        var iCs = ts.insertionCandidates(controls.length);
        List<List<Integer>> insCandidates = iCs.get(1);
        var candidates = ts.findBestInsertionCandidate(route, Set.of(3, 9), insCandidates);
        System.out.println("candidates = " + candidates);
        assertTrue(candidates.size() > 0);
        assertTrue(insCandidates.contains(candidates));
    }

    @Test
    public void testDoSearch() {
        var ts = new OrienteeringProblemSolver(controls, values, 85);
        var best = ts.solve(20000);
        System.out.println("best = " + best);
        System.out.println("ts.calculateScore(best) = " + ts.calculateScore(best));
        System.out.println("ts.calculateTour() = " + ts.calculateTour(best));
        assertTrue(ts.calculateTour(best) < 85);
    }
    @Test
    public void testDoSearchAll() {
        var ts = new OrienteeringProblemSolver(controls, values, 500);
        var best = ts.solve(20000);
        System.out.println("best = " + best);
        assertEquals(controls.length + 1, best.size());
    }

    @Test
    public void testStability() {
        var ts = new OrienteeringProblemSolver(controls, values, 185);
        var best = ts.solve(2000);
        int badScore = 0;
        int badDist = 0;
        for (int i = 0; i < 100; i++) {
            var best2 = ts.solve(2000);
            if(ts.calculateScore(best) != ts.calculateScore(best2)) {
                badScore++;
            }
            if(ts.calculateTour(best) != ts.calculateTour(best2)) {
                badDist++;
            }
        }
        assertEquals(0, badScore);
        assertEquals(0, badDist);
        //assertEquals(controls.length + 1, best.size());
    }

    @Test
    public void testDoBigSearch() {
        var ts = new OrienteeringProblemSolver(bigControls, bigValues, 155);
        var best = ts.solve(20000);
        System.out.println("best = " + best);
        System.out.println("ts.calculateScore(best) = " + ts.calculateScore(best));
        System.out.println("ts.calculateTour() = " + ts.calculateTour(best));
        assertEquals(bigControls.length + 1, best.size());
    }

    @Test
    public void testGH() {

        var lonLats = new double[][]{
                {-1.461176,53.223483},
                {-1.458126,53.222792},
                {-1.452054,53.220857},
                {-1.449878,53.224245},
                {-1.449772,53.228135},
                {-1.454297,53.232216},
                {-1.448520,53.234109},
                {-1.445041,53.236390},
                {-1.449558,53.239618},
                {-1.452529,53.239700},
                {-1.457558,53.239781},
                {-1.456682,53.238542},
                {-1.458622,53.234985},
                {-1.461751,53.234051},
                {-1.462760,53.231137},
                {-1.463736,53.229827},
                {-1.463593,53.228007},
                {-1.464246,53.226696},
                {-1.467745,53.224386},
                {-1.464592,53.222331},
                {-1.463181,53.224232}
        };


        List<ControlSite> controls = new ArrayList<>();
        for (int i = 0; i < lonLats.length; i++) {
            double[] lonLat = lonLats[i];
            var point1 = new GHPoint(lonLat[1], lonLat[0]);
            var cs1 = new ControlSite(point1, "desc1");
            cs1.setValue(2000);
            cs1.setNumber("%2d".formatted(i));
            controls.add(cs1);
        }
        Collections.shuffle(controls.subList(1, controls.size()));
        System.out.println("initial = " +controls.stream().map(ControlSite::getNumber).collect(Collectors.joining(",")));
        controls.get(0).setValue(0);

        GhWrapper wrapper = new GhWrapper();
        var gh = wrapper.loadGH("/home/jon/.local/share/StreetO/derbyshire-latest");
        var csf = new ControlSiteFinder(gh, new StreetOPreferences());
        var ts = new OrienteeringProblemSolver(csf);
        var best = ts.solve(controls, 5000.0, 2000000);
        System.out.println("best = " + best.path().stream().map(ControlSite::getNumber).collect(Collectors.joining(",")));
        System.out.println("ts.calculateScore(best) = " + best.score());
        System.out.println("ts.calculateTour() = " + best.distance());
        //assertEquals(controls.size() + 1, best.path().size());
    }
}

