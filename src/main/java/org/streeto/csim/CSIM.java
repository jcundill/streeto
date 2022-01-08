package org.streeto.csim;

import com.graphhopper.ResponsePath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.streeto.utils.CollectionHelpers.dropFirstAndLast;
import static org.streeto.utils.CollectionHelpers.iterableAsStream;


public class CSIM {

    private final Map<String, Boolean> CA = new HashMap<>();
    private final Map<String, Boolean> CB = new HashMap<>();
    private final Map<String, Boolean> CAd = new HashMap<>();
    private final Map<String, Boolean> CBd = new HashMap<>();
    private double csim;
    private double cincAB;
    private double cincBA;
    private int intAB;
    private int intAdB;
    private int intABd;

    public CSIM(int cellLength) {
        Parameters.PLOT_LENGTH = cellLength;
        Parameters.MAX_CELLS_PER_ZONE = 100000 / Parameters.PLOT_LENGTH;
        Parameters.PLOT_LENGTH_DEGREES = Parameters.PLOT_LENGTH * 0.00020 / 25;

        intAB = 0;
        intAdB = 0;
        intABd = 0;
    }

    private double getInclusionAB() {
        return cincAB;
    }

    private double getInclusionBA() {
        return cincBA;
    }

    private double getSimilarity() {
        return csim;
    }

    public int getIntAB() {
        return intAB;
    }

    public int getIntAdB() {
        return intAdB;
    }

    public int getIntABd() {
        return intABd;
    }

    private Route fromResponsePath(ResponsePath path) {
        var points = dropFirstAndLast(iterableAsStream(path.getPoints()).map(Point::fromGraphHopperPoint).toList(), 1);
        return new Route(points);
    }


    public double calculateFor(ResponsePath a, ResponsePath b) {
        List<Cell> cellsA = fromResponsePath(a).getCells();
        List<Cell> cellsB = fromResponsePath(b).getCells();

        cellsA.forEach(cell -> {
            if (cell.frequency > 0) {
                CA.put(cell.code(), true);
            } else {
                CAd.put(cell.code(), true);
            }
        });

        cellsB.forEach(cell -> {
            if (cell.frequency > 0) {
                CB.put(cell.code(), true);
                if (CA.containsKey(cell.code())) {
                    intAB++;
                } else if (CAd.containsKey(cell.code())) {
                    intAdB++;
                }
            } else {
                CBd.put(cell.code(), true);
                if (CA.containsKey(cell.code())) {
                    intABd++;
                }
            }
        });


        csim = 1.0 * (intAB + intAdB + intABd) / (CA.size() + CB.size() - intAB);
        cincAB = 1.0 * (intAB + intABd) / (CA.size());
        cincBA = 1.0 * (intAB + intAdB) / (CB.size());

        return csim;
    }
}
