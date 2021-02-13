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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.dem.SRTMProvider;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;


public class GhWrapper {

    private final FlagEncoder oFlagEncoder = new StreetOFlagEncoder();

    private GraphHopper initGH(String pbf, String osmDirectory) {
        var name = pbf.split("\\.")[0];
        var location = String.format("%s/grph_%s", osmDirectory, name);
        var gh = new GraphHopperOSM()
                .setOSMFile(pbf)
                .forServer()
                .setGraphHopperLocation(location)
                .setEnableCalcPoints(true)
                .setCHEnabled(false)
                .setElevation(true)
                .setElevationProvider(new SRTMProvider());
        if (!oFlagEncoder.isRegistered()) {
            gh.setEncodingManager(EncodingManager.create(oFlagEncoder));
        }
        gh.importOrLoad();
        return gh;
    }

    public GraphHopper initGH(String name) {
        var osmFile = String.format("extracts/%s.osm.pbf", name);
        var graphHopperLocation = String.format("osm_data/grph_%s", name);
        return initGH(osmFile, graphHopperLocation);
    }

}