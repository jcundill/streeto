package org.streeto.tsp;

import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.*;

import static java.lang.Math.*;

public class OrienteeringProblemSolver {

    public record Result( int score, double distance, List<ControlSite> path){}

    private static final double LARGE_NUMBER = 9999999.0;
    private static final double SMALL_NUMBER = -1 * LARGE_NUMBER;
    private int[] values;
    private double[][] distances;
    private double maxTourLength;
    private double[][] controls;
    private final Random random = new Random();
    private final ControlSiteFinder csf;

    public OrienteeringProblemSolver(ControlSiteFinder csf) {
        this.csf = csf;
//        this.controls = locations;
//        this.distances = calcDistances(locations);
//        this.values = values;
//        this.maxTourLength = maxDistance;
    }

    OrienteeringProblemSolver(double[][] locations, int[] values, double maxTourLength) {
        this.controls = locations;
        this.distances = calcDistances(locations);
        this.values = values;
        this.maxTourLength = maxTourLength;
        csf = null;
    }

    public Result solve(List<ControlSite> controlSites, double maxTourLength, int maxIterations) {
        double [][] locations = new double[controlSites.size()][2];
        int [] values = new int[controlSites.size()];
        for (int i = 0; i < controlSites.size(); i++) {
            var location = controlSites.get(i).getLocation();
            locations[i][0] = location.lat;
            locations[i][1] = location.lon;
            values[i] = controlSites.get(i).getValue();
        }
        this.controls = locations;
        this.distances = calcDistances(controlSites);
        this.values = values;
        this.maxTourLength = maxTourLength;

        var route = solve(maxIterations);
        var distance = calculateTour(route);
        var score = calculateScore(route);
        return new Result(score, distance, route.stream().map(controlSites::get).toList());
    }

    private double[][] calcDistances(List<ControlSite> controlSites) {
        var distances = new double[controlSites.size()][controlSites.size()];
        for (int i = 0, s = controlSites.size(); i < s; i++) {
            for (int j = i; j < s; j++) {
                var resp = csf.routeRequest(List.of(controlSites.get(i), controlSites.get(j)));
                distances[i][j] = resp.getBest().getDistance();
                distances[j][i] = distances[i][j];
            }
        }
        return distances;
    }

    public List<Integer> solve(int maxIterations) {
        // set up
        List<Integer> route = initialization(controls.length);
        var insCandidatesAll = insertionCandidates(controls.length);
        //var tabuList = new ArrayList<Integer>();//
        var tabuList = new HashMap<Integer, Integer>();
        var iterationOfLastImprovement = 0;

        // start with initial route
        var bestRoute = route;
        var bestObj = calculateObjectiveFunction(bestRoute);

        List<Integer> currRoute = new ArrayList<>(route);
        for (int i = 0; i < maxIterations; i++) {
            // insertion candidates, randomly pick a set of insertion candidates
            var randomICIndex = random.nextInt(insCandidatesAll.size());
            var insCandidates = insCandidatesAll.get(randomICIndex);

            List<List<Integer>> delCandidates;
            if (currRoute.size() <= 3) {
                delCandidates = List.of(List.of());
            } else {
                delCandidates = deletionCandidates(currRoute);
            }

            var candidateRoute = new ArrayList<Integer>();
            var tabuAddition = new ArrayList<Integer>();

            // choose the best insertion candidate from the selected ones, then calculate its gain
            var bestInsCandidate = findBestInsertionCandidate(currRoute, tabuList.keySet(), insCandidates);

            var insertedRoute = new ArrayList<>(currRoute);
            var additionalValue = 0.0;
            var additionalDistance = 0.0;
            Collections.shuffle(bestInsCandidate);
            // insert the best insertion candidate into the current  route
            for (var control : bestInsCandidate) {
                if (!insertedRoute.contains(control) && !tabuList.containsKey(control)) {
                    additionalValue += values[control];
                    var minDist = LARGE_NUMBER;
                    var tempRoute = new ArrayList<>(insertedRoute);
                    // find the best place to insert the control in the current route
                    for (int j = 1; j < tempRoute.size() - 1; j++) {
                        var newRoute = insert(insertedRoute, j, control);
                        var diffDist = distances[tempRoute.get(j)][control] + distances[control][tempRoute.get(j + 1)] - distances[tempRoute.get(j)][tempRoute.get(j + 1)];
                        if (diffDist < minDist && calculateTour(newRoute) < maxTourLength) {
                            minDist = diffDist;
                            tempRoute = new ArrayList<>(newRoute);
                        }
                    }
                    insertedRoute = new ArrayList<>(tempRoute);
                    additionalDistance += minDist;
                }
            }
            if (additionalDistance == 0.0) {
                additionalDistance = LARGE_NUMBER;
            }
            // calc gain from insertion
            var insertedObj = additionalValue / additionalDistance;

            // calculate the gain from removing the deletion candidates
            var deletedRoute = new ArrayList<>(currRoute);
            var maxDeletedObj = SMALL_NUMBER;
            for (var delCandidate : delCandidates) {
                var tempRoute = new ArrayList<>(currRoute);
                var deletedValue = 0.0;
                var deletedDistance = 0.0;
                for (var control : delCandidate) {
                    if (tempRoute.contains(control)) {
                        var cPrev = tempRoute.get(tempRoute.indexOf(control) - 1);
                        var cNext = tempRoute.get(tempRoute.indexOf(control) + 1);
                        deletedValue += values[control];
                        deletedDistance += distances[cPrev][control] + distances[control][cNext] - distances[cPrev][cNext];
                        tempRoute.remove(control);
                    }
                }
                if (deletedValue != 0.0 && (deletedDistance / deletedValue > maxDeletedObj)) {
                    maxDeletedObj = deletedDistance / deletedValue;
                    deletedRoute = new ArrayList<>(tempRoute);
                    tabuAddition = new ArrayList<>(delCandidate);
                }
            }
            var deletedObj = maxDeletedObj;

            boolean chooseDelete = true;
            if (insertedObj > deletedObj) {
                candidateRoute = new ArrayList<>(insertedRoute);
                chooseDelete = false;
            } else {
                candidateRoute = new ArrayList<>(deletedRoute);
            }

            updateTabuListTTL(tabuList);
            if (chooseDelete) {
                addToTabuList(route, tabuList, tabuAddition);
            }

            currRoute = new ArrayList<>(candidateRoute);

            //if (i % 5 == 0) {
            currRoute = twoOpt(currRoute);
            //}

            // update the best found so far
            if (calculateObjectiveFunction(currRoute) > bestObj && calculateTour(currRoute) < maxTourLength) {
                iterationOfLastImprovement = i;
                // threeOpt the route here
                currRoute = threeOpt(currRoute);
                bestRoute = new ArrayList<>(currRoute);
                bestObj = calculateObjectiveFunction(currRoute);
            }

            // shuffle if the route is not improving
            if (route.size() > 2 && i - iterationOfLastImprovement > 100) {
                tabuList.clear();
                var tempRoute = route.subList(1, route.size() - 1);
                Collections.shuffle(tempRoute);
                currRoute = new ArrayList<>();
                currRoute.add(0);
                currRoute.addAll(tempRoute);
                currRoute.add(0);
                iterationOfLastImprovement = i;
            }
        }
        return bestRoute;
    }


    private static double calculateDist(double x1, double y1, double x2, double y2) {
        return sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
    }

    private double[][] calcDistances(double[][] locations) {
        var distances = new double[locations.length][locations.length];
        for (int i = 0, s = locations.length; i < s; i++) {
            for (int j = 0; j < s; j++) {
                distances[i][j] = calculateDist(locations[i][0], locations[i][1], locations[j][0], locations[j][1]);
            }
        }
        return distances;
    }

    public double calculateObjectiveFunction(List<Integer> route) {
        if (route.size() == 0) {
            return SMALL_NUMBER;
        }
        var dist = calculateTour(route);
        if (dist > maxTourLength) {
            return SMALL_NUMBER;
        } else if( dist < 5.0) {
            return SMALL_NUMBER;
        } else {
            return calculateScore(route) - dist;
        }
    }

    public double calculateTour(List<Integer> route) {
        double dist = 0;

        for (int i = 1, s = route.size(); i < s; i++) {
            int curr = route.get(i);
            int prev = route.get(i - 1);
            dist += distances[prev][curr];
        }
        return dist;
    }

    public List<Integer> twoOpt(List<Integer> current) {
        int[] route = new int[current.size()];
        for (int i = 0, s = current.size(); i < s; i++) {
            route[i] = current.get(i);
        }

        while (true) {
            int[] tempRoute = new int[route.length];
            System.arraycopy(route, 0, tempRoute, 0, route.length);
            double bestLengthReduction = 0.0;
            for (int i = 1, s = route.length; i < s - 2; i++) {
                for (int j = i + 1; j < s - 1; j++) {
                    int[] newRoute = new int[route.length];
                    System.arraycopy(route, 0, newRoute, 0, route.length);
                    newRoute[i] = route[j];
                    newRoute[j] = route[i];
                    double originalDist = distances[route[i - 1]][route[i]] + distances[route[j]][route[j + 1]];
                    double newDist = distances[route[i - 1]][newRoute[i]] + distances[newRoute[j]][newRoute[j + 1]];
                    double lengthReduction = originalDist - newDist;
                    if (lengthReduction > bestLengthReduction) {
                        System.arraycopy(newRoute, 0, tempRoute, 0, s);
                        bestLengthReduction = lengthReduction;
                    }
                }
            }
            if (bestLengthReduction > 0.1) {
                System.arraycopy(tempRoute, 0, route, 0, route.length);
            } else {
                break;
            }
        }
        List<Integer> result = new ArrayList<>();
        for (int j : route) {
            result.add(j);
        }
        return result;
    }

    public List<Integer> threeOpt(List<Integer> current) {
        int[] route = new int[current.size()];
        for (int i = 0, s = current.size(); i < s; i++) {
            route[i] = current.get(i);
        }

        int[] tempRoute = new int[route.length];
        System.arraycopy(route, 0, tempRoute, 0, route.length);
        double bestLengthReduction = 0.0;
        for (int i = 1, s = route.length; i < s - 3; i++) {
            for (int j = i + 1; j < s - 2; j++) {
                for (int k = j + 1; k < s - 1; k++) {
                    int[] newRoute = new int[route.length];
                    System.arraycopy(route, 0, newRoute, 0, route.length);
                    newRoute[i] = route[k];
                    newRoute[j] = route[j];
                    newRoute[k] = route[i];
                    double originalDist = distances[route[i - 1]][route[i]] + distances[route[j]][route[j + 1]] + distances[route[k]][route[k + 1]];
                    double newDist = distances[route[i - 1]][newRoute[i]] + distances[newRoute[j]][newRoute[j + 1]] + distances[newRoute[k]][newRoute[k + 1]];
                    double lengthReduction = originalDist - newDist;
                    if (lengthReduction > bestLengthReduction) {
                        System.arraycopy(newRoute, 0, tempRoute, 0, s);
                        bestLengthReduction = lengthReduction;
                    }
                }
            }
        }
        if (bestLengthReduction > 0.1) {
            System.arraycopy(tempRoute, 0, route, 0, route.length);
        }

        List<Integer> result = new ArrayList<>();
        for (int j : route) {
            result.add(j);
        }
        return result;
    }

    public List<Integer> initialization(int N) {
        var bestObjs = new ArrayList<Double>();
        var bestRoutes = new ArrayList<List<Integer>>();
        for (int i = 0; i < N / 2; i++) {
            var currentObjectiveFunction = SMALL_NUMBER;
            var currentRoute = new ArrayList<Integer>();
            for (int t = 0; t < 5; t++) {
                var cycleRoute = new ArrayList<Integer>();
                cycleRoute.add(0);
                cycleRoute.add(0);
                for (int j = 0; j < i; j++) {
                    var minObjectiveFunction = LARGE_NUMBER;
                    var k = random.nextInt(cycleRoute.size() - 1);
                    var tempRoute = new ArrayList<>(cycleRoute);
                    for (int control = 1; control < controls.length; control++) {
                        if (!cycleRoute.contains(control)) {
                            var newRoute = insert(cycleRoute, k + 1, control);
                            var objectiveFunctionDiff = (distances[cycleRoute.get(k)][control] + distances[control][cycleRoute.get(k + 1)] - distances[cycleRoute.get(k)][cycleRoute.get(k + 1)]) - values[control];
                            if (objectiveFunctionDiff < minObjectiveFunction && calculateTour(newRoute) < maxTourLength) {
                                tempRoute = new ArrayList<>(newRoute);
                                minObjectiveFunction = objectiveFunctionDiff;
                            }
                        }
                    }
                    cycleRoute = new ArrayList<>(tempRoute);
                }
                var temp_route = twoOpt(cycleRoute);
                var temp_obj = calculateObjectiveFunction(temp_route);
                if (temp_obj > currentObjectiveFunction) {
                    currentObjectiveFunction = temp_obj;
                    currentRoute = new ArrayList<>(temp_route);
                }
            }
            bestRoutes.add(currentRoute);
            bestObjs.add(currentObjectiveFunction);
        }
        var route = new ArrayList<>(bestRoutes.get(0));
        var ratio = 0.0;
        for (int i = 1; i < bestRoutes.size(); i++) {
            if (bestObjs.get(i) / bestRoutes.get(i).size() > ratio) {
                ratio = bestObjs.get(i) / bestRoutes.get(i).size();
                route = new ArrayList<>(bestRoutes.get(i));
            }
        }
        return route;
    }

    private List<Integer> insert(List<Integer> route, int position, int value) {
        var new_route = new ArrayList<Integer>();
        for (int i = 0; i < position; i++) {
            new_route.add(route.get(i));
        }
        new_route.add(value);
        for (int i = position; i < route.size(); i++) {
            new_route.add(route.get(i));
        }
        return new_route;
    }

    public double dispersionIndex(List<Integer> cluster) {
        if (cluster.size() == 1) {
            return 0;
        } else {
            var sm = 0.0;
            for (int c1 : cluster) {
                for (int c2 : cluster) {
                    sm = sm + distances[c1][c2];
                }
            }
            return sm / (cluster.size() * (cluster.size() - 1));
        }
    }

    public double proximityMeasure(List<Integer> cluster1, List<Integer> cluster2) {
        var sm = 0.0;
        for (int c1 : cluster1) {
            for (int c2 : cluster2) {
                sm = sm + distances[c1][c2];
            }
        }
        return (2.0 / (cluster1.size() * cluster2.size())) * sm - dispersionIndex(cluster1) - dispersionIndex(cluster2);
    }

    public List<List<List<Integer>>> insertionCandidates(int N) {
        var rList = List.of(1, (int) (N / 2.0), (int) (2 * N / 3.0), (int) (3 * N / 4.0), (int) (4 * N / 5.0), (int) (5 * N / 6.0), (int) (6 * N / 7.0), (int) (7 * N / 8.0), (int) (8 * N / 9.0), (int) (9 * N / 10.0));

        var candidates = new ArrayList<List<List<Integer>>>();
        var partition = new ArrayList<List<Integer>>();
        for (int i = 1; i < N; i++) {
            partition.add(List.of(i));
        }
        candidates.add(new ArrayList<>(partition));

        for (int r = 2; r < N; r++) {
            var minProximity = LARGE_NUMBER;
            var minProximityI = 0;
            var minProximityJ = 0;
            for (int i = 0; i < N - 1; i++) {
                boolean merged = false;
                for (int j = i + 1; j < partition.size(); j++) {
                    var pM = proximityMeasure(partition.get(i), partition.get(j));
                    if (pM < minProximity) {
                        minProximity = pM;
                        minProximityI = i;
                        minProximityJ = j;
                        merged = true;
                    }
                }
                if (merged) {
                    var a = partition.get(minProximityI);
                    var b = partition.get(minProximityJ);
                    partition.remove(minProximityJ);
                    partition.remove(minProximityI);
                    var cluster = new ArrayList<Integer>();
                    cluster.addAll(a);
                    cluster.addAll(b);
                    partition.add(cluster);
                    if (rList.contains(r) && !(partition.size() == 1)) {
                        candidates.add(new ArrayList<>(partition));
                    }
                }
            }
        }
        return candidates;
    }

    public List<List<Integer>> deletionCandidates(List<Integer> route) {
        record Edge(double weight, int i, int j) {
        }

        var candidates = new ArrayList<List<Integer>>();

        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < route.size() - 1; i++) {
            var edge = new Edge(distances[route.get(i)][route.get(i + 1)], i, i + 1);
            edges.add(edge);
        }
        edges.sort(Comparator.comparingDouble(x -> -1 * x.weight));

        // take the best K-1 edges
        var K = random.nextInt(max(2, (route.size() / 4))) + 2;
        edges = edges.subList(0, K);

        for (int i = 0; i < K - 1; i++) {
            var tempList = new ArrayList<Integer>();
            for (int j = edges.get(i).j; j <= edges.get(i + 1).i; j++) {
                tempList.add(route.get(j));
            }
            if (!tempList.isEmpty()) {
                candidates.add(new ArrayList<>(tempList));
            }
        }
        return candidates;
    }

    public List<Integer> findBestInsertionCandidate(List<Integer> route, Set<Integer> tabuList, List<List<Integer>> insCandidates) {
        var bestInsCandidate = new ArrayList<Integer>();
        var bestInsObj = SMALL_NUMBER;

        for (var iC : insCandidates) {
            var profitSum = 0.0;
            var gCenter = new double[]{0, 0};
            for (var c : iC) {
                if (!route.contains(c) && !tabuList.contains(c)) {
                    gCenter[0] += controls[c][0] / iC.size();
                    gCenter[1] += controls[c][1] / iC.size();
                    profitSum += values[c];
                }
            }
            var minDist = LARGE_NUMBER;
            for (int j = 0; j < route.size() - 1; j++) {
                var distAdd1 = calculateDist(controls[route.get(j)][0], controls[route.get(j)][1], gCenter[0], gCenter[1]);
                var distAdd2 = calculateDist(gCenter[0], gCenter[1], controls[route.get(j + 1)][0], controls[route.get(j + 1)][1]);
                var distRem = calculateDist(controls[route.get(j)][0], controls[route.get(j)][1], controls[route.get(j + 1)][0], controls[route.get(j + 1)][1]);

                var dist = distAdd1 + distAdd2 - distRem;
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (profitSum / minDist > bestInsObj) {
                bestInsObj = profitSum / minDist;
                bestInsCandidate = new ArrayList<>(iC);
            }
        }
        return bestInsCandidate;
    }


    private void addToTabuList(List<Integer> route, HashMap<Integer, Integer> tabuList, ArrayList<Integer> tabuAddition) {
        // put any deletions in the tabu list
        for (var tA : tabuAddition) {
            if (route.contains(tA)) {
                var tabuTime = random.nextInt(20) + 5;
                tabuList.put(tA, random.nextInt(tabuTime));
            }
        }
    }

    private void updateTabuListTTL(HashMap<Integer, Integer> tabuList) {
        //update the tabu list each time through the loop
        // decrease the tabu time for each control in the tabu list
        if (tabuList.size() > 0) {
            var removalSet = new HashSet<Integer>();
            for (var key : tabuList.keySet()) {
                if (tabuList.get(key) > 0) {
                    tabuList.put(key, tabuList.get(key) - 1);
                } else {
                    removalSet.add(key);
                }
            }
            for (var key : removalSet) {
                tabuList.remove(key);
            }
        }
    }

    int calculateScore(List<Integer> route) {
        int score = 0;
        for (Integer control : route) {
            score += values[control];
        }
        return score;
    }
}

