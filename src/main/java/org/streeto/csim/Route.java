package org.streeto.csim;

import org.jetbrains.annotations.NotNull;
import org.streeto.utils.CollectionHelpers;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static org.streeto.csim.MGRSConverter.MGRSCell;
import static org.streeto.csim.MGRSConverter.convert;


class Route {
    private static final double INTERPOLATION_INC = 0.00025;
    public final double plotLengthDegrees;
    private final int cellLength;
    private final int cellsPerZone;

    public Route(int cellLength) {
        this.cellLength = cellLength;
        this.cellsPerZone = 100000 / cellLength;//4000;// depends on  100000 / Plot Length
        this.plotLengthDegrees = cellLength * 0.00020 / 25;
    }

    public List<Cell> getCells(List<Point> points) {
        List<Cell> plot = addTrackToPlot(points);
        List<Cell> interpolated = interpolate(plot);
        return dilate(interpolated);
    }

    @NotNull
    /*
      add mercator points of route to plot cells
     */
    private List<Cell> addTrackToPlot(List<Point> points) {
        List<Cell> trackCells = new ArrayList<>();
        for (Point value : points) {
            var point = convert(value.getLatitude(), value.getLongitude());
            var pointCell = getCellForPoint(point, false, cellLength);
            if (!trackCells.contains(pointCell)) {
                trackCells.add(pointCell);
            } else {
                trackCells.get(trackCells.indexOf(pointCell)).increaseFrequency();
            }
        }
        //printCells(trackCells);
        return trackCells;
    }

    /*
    Interpolating cells...
     */
    private List<Cell> interpolate(List<Cell> plots) {
        var pairs = CollectionHelpers.windowed(plots, 2)
                .map(p -> new CellPair(p.get(0), p.get(1)))
                .toList();

        for (var cellPair : pairs) {
            Cell first = cellPair.first;
            Cell second = cellPair.second;

            if (!first.zone.equals(second.zone)) { //
                first.updateCellLocation();
                second.updateCellLocation();
                double deltaX = abs(first.latitude - second.latitude);
                double deltaY = abs(first.longitude - second.longitude);
                double minX = min(first.latitude, second.latitude);
                double minY = min(first.longitude, second.longitude);
                int flipX = first.latitude > second.latitude ? 1 : 0;
                int flipY = first.longitude > second.longitude ? 1 : 0;

                for (double j = INTERPOLATION_INC; j < max(deltaX, deltaY); j += INTERPOLATION_INC) {
                    double[] loc = getInterpolatedLocation(minX, minY, deltaX, deltaY, flipX, flipY, j);
                    var interpolatedPoint = convert(loc[0], loc[1]);
                    var interpolatedCell = getCellForPoint(interpolatedPoint, false, cellLength);
                    if (!plots.contains(interpolatedCell)) {
                        plots.add(interpolatedCell);
                    }
                }
            } else {
                double deltaX = second.X - first.X;
                double deltaY = second.Y - first.Y;

                double hypotenuse = sqrt(pow(deltaX, 2) + pow(deltaY, 2));
                double xRatio = deltaX / hypotenuse;
                double yRatio = deltaY / hypotenuse;

                for (double i = 1.0; i < hypotenuse; i += 1.0) {
                    double newX = first.X + (i * xRatio);
                    double newY = first.Y + (i * yRatio);
                    var interpolatedCell = new Cell((int) round(newX), (int) round(newY), true, first.zone, cellLength);
                    if (!plots.contains(interpolatedCell)) {
                        plots.add(interpolatedCell);
                    }
                }
            }
        }
        //printCells(plots);
        return plots;
    }

    private void printCells(List<Cell> plots) {
        var minX = plots.stream().mapToInt(c -> c.X).min().orElse(0);
        var minY = plots.stream().mapToInt(c -> c.Y).min().orElse(0);
        var maxX = plots.stream().mapToInt(c -> c.X).max().orElse(0);
        var maxY = plots.stream().mapToInt(c -> c.Y).max().orElse(0);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                int finalX = x;
                int finalY = y;
                System.out.print("|" + (plots.stream().anyMatch(c -> (c.X == finalX) && (c.Y == finalY)) ? "X" : " "));
            }
            System.out.println("|");
        }
        System.out.println();
    }

    private double[] getInterpolatedLocation(double minX, double minY, double deltaX, double deltaY, int flipX, int flipY, double j) {
        double xLoc = minX + (1 - flipX) * j + flipX * (deltaX - j);
        double yLoc = minY + j * deltaY / deltaX * (1 - flipY) + (deltaX - j * deltaY / deltaX) * flipY;
        if (deltaX <= deltaY) {
            xLoc = minX + j * deltaX / deltaY * (1 - flipX) + (deltaY - j) * deltaX / deltaY * flipX;
            yLoc = minY + j * (1 - flipY) + (deltaY - j) * flipY;
        }
        return new double[]{xLoc, yLoc};
    }

    private List<Cell> dilate(List<Cell> cells) {
        List<Cell> shadowCells = new ArrayList<>();

        for (Cell cell : cells) {
            if (cell.X == 0 || cell.X == cellsPerZone - 1 || cell.Y == 0 || cell.Y == cellsPerZone - 1) {
                cell.updateCellLocation();

                var point = convert(cell.latitude, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude + plotLengthDegrees, cell.longitude);
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude - plotLengthDegrees, cell.longitude);
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude + plotLengthDegrees, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude + plotLengthDegrees, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude - plotLengthDegrees, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);

                point = convert(cell.latitude - plotLengthDegrees, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
            } else {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (!(i == 0 && j == 0)) {
                            Cell shadowCell = new Cell(cell.X + i, cell.Y + j, cell.fake, cell.zone, cellLength);
                            if (!shadowCells.contains(shadowCell)) {
                                shadowCells.add(shadowCell);
                            }
                        }
                    }
                }
            }
        }
        for (Cell shadowPlot : shadowCells) {
            shadowPlot.frequency = 0;
            if (!cells.contains(shadowPlot)) {
                cells.add(shadowPlot);
            }
        }
        //printCells(cells);
        return cells;
    }

    private void addToShadowCells(List<Cell> shadowPlots, Cell plot, MGRSCell point) {
        Cell shadowPlot = getCellForPoint(point, true, cellLength);
        shadowPlot.fake = plot.fake;
        shadowPlot.frequency = 0;
        if (!shadowPlots.contains(shadowPlot)) {
            shadowPlots.add(shadowPlot);
        }
    }

    private Cell getCellForPoint(MGRSCell point, boolean isFake, int length) {
        int X = point.x / length;
        int Y = point.y / length;
        return new Cell(X, Y, isFake, point.zone, cellLength);
    }

    private static class CellPair {
        public Cell first;
        public Cell second;

        public CellPair(Cell a, Cell b) {
            first = a;
            second = b;
        }
    }

}
