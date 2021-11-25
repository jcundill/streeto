package org.streeto;

import org.streeto.gpx.GpxFacade;
import org.streeto.kml.KmlWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;

public class CourseImporter {
    private final ControlSiteFinder csf;

    public CourseImporter(ControlSiteFinder csf) {
        this.csf = csf;
    }

    public Course buildFromProperties(Properties props) {
        var waypoints = props.getProperty("controls", "");
        var distance = Double.parseDouble(props.getProperty("distance", "6000.0"));
        var numControls = Integer.parseInt(props.getProperty("numControls", "10"));

        var initials = new ArrayList<ControlSite>();
        if (!waypoints.equals("")) {
            for (String it : waypoints.split("\\|")) {
                var latlon = it.split(",");
                var site = new ControlSite(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]), "_initial_");
                initials.add(site);
            }

        }
        return new Course(distance, numControls, initials);
    }

    public Course buildFromKml(InputStream is) throws Exception {
        var points = new KmlWriter().parseStream(is).collect(Collectors.toList());
        var numControls = points.size() - 2;
        var distance = csf.routeRequest(points).getBest().getDistance();
        return new Course(distance, numControls, points);
    }

    public Course buildFromGPX(String filename) throws IOException {
        return GpxFacade.readCourse(filename);
    }


}
