package org.streeto.csim;

import gov.nasa.worldwind.geom.coords.MGRSCoordConverter;
import org.jetbrains.annotations.NotNull;
import org.streeto.utils.CollectionHelpers;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;


class Route {
    private static final double INTERPOLATION_INC = 0.00025;
    public final double plotLengthDegrees;
    private final int cellLength;
    private final int cellsPerZone;
    private final MGRSCoordConverter mgrs = new MGRSCoordConverter();

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
            Point point = convertLatLonToMGRS(value.getLatitude(), value.getLongitude());
            Cell pointCell = getCellForPoint(point, cellLength);
            if (!trackCells.contains(pointCell)) {
                trackCells.add(pointCell);
            } else {
                trackCells.get(trackCells.indexOf(pointCell)).increaseFrequency();
            }
        }
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

                for (double j = INTERPOLATION_INC; j < deltaX; j += INTERPOLATION_INC) {
                    double[] loc = getInterpolatedLocation(minX, minY, deltaX, deltaY, flipX, flipY, j);
                    Point interpolatedPoint = convertLatLonToMGRS(loc[0], loc[1]);
                    Cell interpolatedCell = getCellForPoint(interpolatedPoint, cellLength);
                    if (!plots.contains(interpolatedCell)) {
                        plots.add(interpolatedCell);
                    }
                }
            } else {
                double deltaX = abs(first.X - second.X);
                double deltaY = abs(first.Y - second.Y);
                double minX = min(first.X, second.X);
                double minY = min(first.Y, second.Y);
                int flipX = first.X > second.X ? 1 : 0;
                int flipY = first.Y > second.Y ? 1 : 0;

                for (double j = 1; j < deltaX; j += 1.0) {
                    double[] loc = getInterpolatedLocation(minX, minY, deltaX, deltaY, flipX, flipY, j);
                    Cell interpolatedCell = new Cell((int) round(loc[0]), (int) round(loc[1]), true, first.zone, cellLength);
                    if (!plots.contains(interpolatedCell)) {
                        plots.add(interpolatedCell);
                    }
                }
            }
        }
        return plots;
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

                Point point = convertLatLonToMGRS(cell.latitude, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude + plotLengthDegrees, cell.longitude);
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude - plotLengthDegrees, cell.longitude);
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude + plotLengthDegrees, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude + plotLengthDegrees, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude - plotLengthDegrees, cell.longitude - plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
                //
                point = convertLatLonToMGRS(cell.latitude - plotLengthDegrees, cell.longitude + plotLengthDegrees * cos(cell.latitude));
                addToShadowCells(shadowCells, cell, point);
            } else {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) {
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
        return cells;
    }

    private void addToShadowCells(List<Cell> shadowPlots, Cell plot, Point point) {
        Cell shadowPlot = getCellForPoint(point, cellLength);
        shadowPlot.fake = plot.fake;
        shadowPlot.frequency = 0;
        if (!shadowPlots.contains(shadowPlot)) {
            shadowPlots.add(shadowPlot);
        }
    }

    private Point convertLatLonToMGRS(double lat, double lon) {
        var error = mgrs.convertGeodeticToMGRS(toRadians(lat),
                toRadians(lon), 5);
        if (error != 0) {
            System.out.println("error = " + error);
        }
        String[] result = mgrs.getMGRSString().split(" ");

        if (result.length < 3) {
            System.out.println("Error converting lat/lon to MGRS");
        }

        String code = result[0];
        int X = Integer.parseInt(result[1]);
        int Y = Integer.parseInt(result[2]);

        return new Point(X, Y, code);
    }

    private Cell getCellForPoint(Point point, int length) {
        int X = (int) (point.getLatitude() / length);
        int Y = (int) (point.getLongitude() / length);
        return new Cell(X, Y, point.isFake(), point.getCode(), cellLength);
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
