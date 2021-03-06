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

package org.streeto.osmdata;

import com.graphhopper.config.Profile;
import com.graphhopper.reader.dem.SRTMProvider;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import org.streeto.StreetOFlagEncoder;


public class GhWrapper {

    private static final FlagEncoder oFlagEncoder = new StreetOFlagEncoder();
    private static final EncodingManager encodingManager = EncodingManager.create(oFlagEncoder);

    private final GraphHopperOSM gh = null;

    public GhWrapper() {
    }

    public GraphHopperOSM loadGH(String osmDirectory) {
        var profile = new Profile("streeto").setVehicle("streeto").setWeighting("fastest").setTurnCosts(false);
        var gh = new GraphHopperOSM()
                .setProfiles(profile)
                .setElevation(true);
        gh.setEncodingManager(encodingManager);
        gh.load(osmDirectory);
        return (GraphHopperOSM) gh;
    }

    public void initGH(String pbf, String osmDirectory) {
        var profile = new Profile("streeto").setVehicle("streeto").setWeighting("fastest").setTurnCosts(false);
        var gh = new GraphHopperOSM()
                .setOSMFile(pbf)
                .forServer()
                .setGraphHopperLocation(osmDirectory)
                .setProfiles(profile)
                .setElevation(true)
                .setElevationProvider(new SRTMProvider());
        gh.setEncodingManager(encodingManager);
        gh.importAndClose();
    }
}