package org.streeto.tsp;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.streeto.utils.CollectionHelpers.*;

public class BestSubsetOfTsp {

    private final ControlSiteFinder csf;

    public BestSubsetOfTsp(ControlSiteFinder finder) {
        this.csf = finder;
    }

    private void formatNumber(ControlSite controlSite, String format) {
        controlSite.setNumber(format);
    }


    public Optional<VehicleRoute> solve(List<ControlSite> controls, int numToVisit, int iterations) {

        var renumbered = controls.stream().map(cs -> new ControlSite(cs.getLocation(), cs.getDescription())).toList();
        formatNumber(first(renumbered), "S1");
        formatNumber(last(renumbered), "F1");
        forEachIndexed(dropFirstAndLast(renumbered, 1), (i, ctrl) -> formatNumber(ctrl, String.format("%d", i + 1)));

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
                .addCapacityDimension(0, numToVisit)
                .setCostPerDistance(1)
                .build();

        var locations = renumbered.stream().map(c -> Location.newInstance(c.getNumber())).toList();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
                .setStartLocation(locations.get(0)).setType(type).build();


        //define a matrix-builder building a symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (int i = 0; i < renumbered.size(); i++) {
            for (int j = i; j < renumbered.size(); j++) {
                costMatrixBuilder.addTransportDistance(renumbered.get(i).getNumber(), renumbered.get(j).getNumber(),
                        csf.routeRequest(List.of(renumbered.get(i), renumbered.get(j)), 0).getBest().getDistance());
            }
        }
        var costMatrix = costMatrixBuilder.build();

        var builder = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costMatrix)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .addVehicle(vehicle);

        for (int i = 1; i < controls.size() - 1; i++) {
            var jobBuilder = Service.Builder.newInstance(renumbered.get(i).getNumber()).setLocation(locations.get(i));
            jobBuilder.addSizeDimension(0, 1);
            builder.addJob(jobBuilder.build());
        }

        var vrp = builder.build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        vra.setMaxIterations(iterations);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        //SolutionPrinter.print(Solutions.bestOf(solutions));
        return Solutions.bestOf(solutions).getRoutes().stream().findFirst();
    }
}
