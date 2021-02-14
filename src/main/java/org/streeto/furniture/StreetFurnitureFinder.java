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

package org.streeto.furniture;

import com.graphhopper.util.shapes.BBox;
import de.westnordost.osmapi.OsmConnection;
import de.westnordost.osmapi.overpass.OverpassMapDataDao;
import org.streeto.ControlSite;

import java.util.ArrayList;
import java.util.List;

public class StreetFurnitureFinder {

    public List<ControlSite> findForBoundingBox(BBox box) {
        var locations = new ArrayList<ControlSite>();
        var connection = new OsmConnection("https://overpass-api.de/api/", "streeto");
        var overpass = new OverpassMapDataDao(connection);
        var handler = new StreetFurnitureMapDataHandler(locations);
        var bbox = String.format("%f,%f,%f,%f",box.minLat, box.minLon, box.maxLat, box.maxLon);
        var q = ("            (\n" +
                 "  node[\"highway\"=\"bus_stop\"](" +bbox +");\n" +
                 "  node[\"highway\"=\"crossing\"](" +bbox + ");\n" +
                 "  node[\"highway\"=\"give_way\"](" +bbox + ");\n" +
                 //"  node[\"highway\"=\"traffic_signals\"](" +bbox + ");\n" + // don't find these
                 "  node[\"tourism\"=\"information\"](" +bbox + ");\n" +
                 "  node[\"tourism\"=\"artwork\"](" +bbox + ");\n" +
                 "  node[\"natural\"=\"tree\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"post_box\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"bbq\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"drinking_water\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"charging_station\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"grit_bin\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"bench\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"telephone\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"vending_machine\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"waste_basket\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"waste_disposal\"](" +bbox + ");\n" +
                 "  node[\"amenity\"=\"water_point\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"gate\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"bollard\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"cycle_barrier\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"kissing_gate\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"horse_stile\"](" +bbox + ");\n" +
                 "  node[\"barrier\"=\"stile\"](" +bbox + ");\n" +
                 "  node[\"historic\"=\"memorial\"](" +bbox + ");\n" +
                 "  node[\"historic\"=\"milestone\"](" +bbox + ");\n" +
                 "  node[\"historic\"=\"boundary_stone\"](" +bbox + ");\n" +
                 "  node[\"historic\"=\"cannon\"](" +bbox + ");\n" +
                 "  way[\"highway\"=\"steps\"](" +bbox + ");\n" +
                 "  way[\"bridge\"=\"yes\"](" +bbox + ");\n" +
                 "  way[\"barrier\"=\"hedge\"](" +bbox + ");\n" +
                 "                <;); out body geom;\n").trim();

        overpass.queryElementsWithGeometry(q, handler);
        return locations;
    }
}