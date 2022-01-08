package org.streeto.csim;

import gov.nasa.worldwind.geom.coords.MGRSCoordConverter;

import java.util.ArrayList;
import java.util.List;


class Route {
    List<Point> points;
    List<Point> mercatorPoints;
    public final double plotLengthDegrees;
    private final int cellLength;
    private final int cellsPerZone;


    public Route(List<Point> points, int cellLength) {
        this.cellLength = cellLength;
        this.cellsPerZone = 100000 / cellLength;//4000;// depends on  100000 / Plot Length
        this.plotLengthDegrees = cellLength * 0.00020 / 25;
        this.points = points;
        this.mercatorPoints = new ArrayList<>();

        for (Point value : this.points) {
            Point point = geodeticToMGRS(value.latitude, value.longitude);
            mercatorPoints.add(point);
        }
    }

    public Point getMercator(int index) {
        return mercatorPoints.get(index);
    }

    public List<Cell> getCells() {
        //System.out.println("Getting cells...");
        List<Cell> plots = new ArrayList<>();
        List<Cell> shadowPlots = new ArrayList<>();
        Cell lastPlot = null;

        // add mercator points of route to plot cells
        List<CellPair> plotPairArray = new ArrayList<>();
        for (Point point : this.mercatorPoints) {
            Cell plot = getPlotForPoint(point, cellLength);
            if (!plots.contains(plot)) {
                plots.add(plot);
            } else {
                plots.get(plots.indexOf(plot)).increaseFrequency();
            }
            if (lastPlot != null && !plot.equals(lastPlot)) {
                plotPairArray.add(new CellPair(lastPlot, plot));
            }
            lastPlot = plot;

        }

        //System.out.println("Interpolating cells...");
        for (CellPair cellPair : plotPairArray) {
            Cell first = cellPair.firstPlot;
            Cell second = cellPair.secondPlot;
            int flipX = 0;
            int flipY = 0;

            if (!first.zone.equals(second.zone)) { //
                first.updateCellLocation();
                second.updateCellLocation();
                double deltaX = Math.abs(first.latitude - second.latitude);
                double deltaY = Math.abs(first.longitude - second.longitude);
                double minX = Math.min(first.latitude, second.latitude);
                double minY = Math.min(first.longitude, second.longitude);

                if (first.latitude > second.latitude) {
                    flipX = 1;
                }
                if (first.longitude > second.longitude) {
                    flipY = 1;
                }


                double inc = 0.00025;
                if (deltaX > deltaY) {
                    for (double j = inc; j < deltaX; j += inc) {

                        MGRSCoordConverter a = new MGRSCoordConverter();
                        a.convertGeodeticToMGRS(Math.toRadians(minX + (1 - flipX) * j + flipX * (deltaX - j)),
                                Math.toRadians(minY + j * deltaY / deltaX * (1 - flipY) + (deltaX - j) * deltaY / deltaX * flipY), 5);
                        String[] result = a.getMGRSString().split(" ");

                        String code = result[0];
                        int X = Integer.parseInt(result[1]);
                        int Y = Integer.parseInt(result[2]);


                        Cell newPlot = getPlotForPoint(new Point(X, Y, code), cellLength);


                        if (!plots.contains(newPlot)) {
                            plots.add(newPlot);
                        }
                    }
                } else {

                    for (double j = inc; j < deltaY; j += inc) {


                        MGRSCoordConverter a = new MGRSCoordConverter();
                        a.convertGeodeticToMGRS(Math.toRadians(minX + (1 - flipX) * j * deltaX / deltaY + flipX * (deltaY - j) * deltaX / deltaY),
                                Math.toRadians(minY + j * (1 - flipY) + (deltaY - j) * flipY), 5);
                        String[] result = a.getMGRSString().split(" ");

                        String code = result[0];
                        int X = Integer.parseInt(result[1]);
                        int Y = Integer.parseInt(result[2]);

                        Cell newPlot = getPlotForPoint(new Point(X, Y, code), cellLength);

                        if (!plots.contains(newPlot)) {
                            plots.add(newPlot);
                        }
                    }
                }

            } else {
                int deltaX = Math.abs(first.X - second.X);
                int deltaY = Math.abs(first.Y - second.Y);
                int minX = Math.min(first.X, second.X);
                int minY = Math.min(first.Y, second.Y);


                if (first.X > second.X) {
                    flipX = 1;
                }
                if (first.Y > second.Y) {
                    flipY = 1;
                }


                if (deltaX > deltaY) {
                    for (int j = 1; j < deltaX; j++) {
                        Cell newPlot = new Cell(minX + j * (1 - flipX) + (deltaX - j) * flipX,
                                minY + Math.round(j * deltaY / deltaX) * (1 - flipY) + Math.round((deltaX - j) * deltaY / deltaX) * flipY,
                                true, first.zone, cellLength);

                        if (!plots.contains(newPlot)) {
                            plots.add(newPlot);
                        }
                    }
                } else {
                    for (int j = 1; j < deltaY; j++) {
                        Cell newPlot = new Cell(minX + Math.round(j * deltaX / deltaY) * (1 - flipX) + Math.round((deltaY - j) * deltaX / deltaY) * flipX,
                                minY + j * (1 - flipY) + (deltaY - j) * flipY,
                                true, first.zone, cellLength);

                        if (!plots.contains(newPlot)) {
                            plots.add(newPlot);
                        }
                    }
                }
            }
        }

        //System.out.println("Dilating cells...");
        for (Cell plot : plots) {
            if (plot.X == 0 || plot.X == cellsPerZone - 1 || plot.Y == 0 || plot.Y == cellsPerZone - 1) {
                plot.updateCellLocation();
                MGRSCoordConverter a = new MGRSCoordConverter();

                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude),
                        Math.toRadians(plot.longitude - plotLengthDegrees * Math.cos(plot.latitude)), 5);
                String[] result = a.getMGRSString().split(" ");
                String code = result[0];
                int X = Integer.parseInt(result[1]);
                int Y = Integer.parseInt(result[2]);
                Cell shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude),
                        Math.toRadians(plot.longitude + plotLengthDegrees * Math.cos(plot.latitude)), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude + plotLengthDegrees),
                        Math.toRadians(plot.longitude), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude - plotLengthDegrees),
                        Math.toRadians(plot.longitude), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude + plotLengthDegrees),
                        Math.toRadians(plot.longitude - plotLengthDegrees * Math.cos(plot.latitude)), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude + plotLengthDegrees),
                        Math.toRadians(plot.longitude + plotLengthDegrees * Math.cos(plot.latitude)), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude - plotLengthDegrees),
                        Math.toRadians(plot.longitude - plotLengthDegrees * Math.cos(plot.latitude)), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                a.convertGeodeticToMGRS(Math.toRadians(plot.latitude - plotLengthDegrees),
                        Math.toRadians(plot.longitude + plotLengthDegrees * Math.cos(plot.latitude)), 5);
                result = a.getMGRSString().split(" ");
                code = result[0];
                X = Integer.parseInt(result[1]);
                Y = Integer.parseInt(result[2]);
                shadowPlot = getPlotForPoint(new Point(X, Y, code), cellLength);
                shadowPlot.fake = plot.fake;
                shadowPlot.frequency = 0;
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
            } else {
                Cell shadowPlot = new Cell(plot.X, plot.Y - 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X, plot.Y + 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X + 1, plot.Y, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X - 1, plot.Y, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X + 1, plot.Y - 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X + 1, plot.Y + 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X - 1, plot.Y - 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
                //
                shadowPlot = new Cell(plot.X - 1, plot.Y + 1, plot.fake, plot.zone, cellLength);
                if (!shadowPlots.contains(shadowPlot)) {
                    shadowPlots.add(shadowPlot);
                }
            }

        }
        for (Cell shadowPlot : shadowPlots) {
            shadowPlot.frequency = 0;
            if (!plots.contains(shadowPlot)) {
                plots.add(shadowPlot);
            }
        }

        return plots;
    }

    private Point geodeticToMGRS(double lat, double lon) {
        var coordConverterA = new MGRSCoordConverter();
        var error = coordConverterA.convertGeodeticToMGRS(Math.toRadians(lat),
                Math.toRadians(lon), 5);
        if (error != 0) {
            System.out.println("error = " + error);
        }
        String[] result = coordConverterA.getMGRSString().split(" ");

        if (result.length < 3) {
            System.out.println("Error converting lat/lon to MGRS");
        }

        String code = result[0];
        int X = Integer.parseInt(result[1]);
        int Y = Integer.parseInt(result[2]);

        return new Point(X, Y, code);
    }

    private Cell getPlotForPoint(Point point, int length) {
        int X = (int) (point.latitude / length);
        int Y = (int) (point.longitude / length);
        return new Cell(X, Y, point.fake, point.code, cellLength);
    }

}
