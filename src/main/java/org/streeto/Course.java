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

import com.graphhopper.PathWrapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;



public class Course {

    private final double requestedDistance;
    private final int requestedNumControls;
    private final List<ControlSite> controls;
    private List<Double> legScores = List.of();
    private Map<String, List<Double>> featureScores = Map.of();
    private double energy = 1000.0;
    private PathWrapper route = null;

    public double getRequestedDistance() {
        return requestedDistance;
    }

    public int getRequestedNumControls() {
        return requestedNumControls;
    }

    public List<ControlSite> getControls() {
        return controls;
    }

    public List<Double> getLegScores() {
        return legScores;
    }

    public Map<String, List<Double>> getFeatureScores() {
        return featureScores;
    }

    public void setLegScores(List<Double> legScores) {
        this.legScores = legScores;
    }

    public void setFeatureScores(Map<String, List<Double>> featureScores) {
        this.featureScores = featureScores;
    }

    public double getEnergy() {
        return energy;
    }

    public PathWrapper getRoute() {
        return route;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setRoute(PathWrapper route) {
        this.route = route;
    }

    public Course(
            double requestedDistance,
            int requestedNumControls,
            List<ControlSite> controls) {
        this.requestedDistance = requestedDistance;
        this.requestedNumControls = requestedNumControls;
        this.controls = controls;
    }

    public Course copy( List<ControlSite> controls) {
        return new Course( this.getRequestedDistance(), this.requestedNumControls, controls);
    }

    public double distance() {

        if( requestedDistance == 0.0) return route.getDistance() * 0.8; // no desired distance given, make it about as long as it is now
        else return requestedDistance;
    }

    @Override
    public String toString() {
        return "Course{" +
               "requestedDistance=" + requestedDistance +
               ", requestedNumControls=" + requestedNumControls +
               ", controls=" + controls +
               '}';
    }

    public static Course buildFromProperties(String filename) {
            var props = new Properties();
        try {
            props.load(new FileInputStream(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        var waypoints = props.getProperty("controls", "");
            var distance = Double.parseDouble(props.getProperty("distance", "6000.0"));
            var numControls = Integer.parseInt(props.getProperty("numControls", "10"));

            var initials = new ArrayList<ControlSite>();
            if( !waypoints.equals("")) {
                for (String it : waypoints.split("\\|")) {
                    var latlon = it.split(",");
                    var site = new ControlSite(Double.parseDouble(latlon[0]),Double.parseDouble(latlon[1]) ,"_initial_");
                    initials.add(site);
                }

            }
            return new Course(distance, numControls, initials);
        }

        private static Course courseFromPoints(
            List<ControlSite> points, Function<List<ControlSite>,Double> measurer) {
            var numControls = points.size() - 2;
            var distance = measurer.apply(points);

            return new Course(distance, numControls, points);
        }
}