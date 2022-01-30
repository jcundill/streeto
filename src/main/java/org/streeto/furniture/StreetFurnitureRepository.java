package org.streeto.furniture;

import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.streeto.ControlSite;
import org.streeto.ControlSiteFinder;
import org.streeto.ControlType;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.streeto.utils.DistUtils.dist;

public class StreetFurnitureRepository {
    private final String osmDir;
    private final ControlSiteFinder csf;
    private final StreetFurnitureFinder finder = new StreetFurnitureFinder();

    public StreetFurnitureRepository(ControlSiteFinder csf, String osmDir) {
        this.osmDir = osmDir;
        this.csf = csf;
    }

    public void loadForLocation(GHPoint location) {
        var maybeFurniture = getForLocation(location);
        if (maybeFurniture.isEmpty()) {
            var scaleFactor = 6000.0;
            var max = csf.getGHPointRelativeTo(location, Math.PI * 0.25, scaleFactor);
            var min = csf.getGHPointRelativeTo(location, Math.PI * 1.25, scaleFactor);
            var bbox = new BBox(min.lon, max.lon, min.lat, max.lat);
            var maybeRetrieved = finder.findForBoundingBox(bbox);
            maybeRetrieved.ifPresent(controlSites -> {
                saveForLocation(bbox, controlSites);
                csf.setFurniture(controlSites);
            });
        } else {
            csf.setFurniture(maybeFurniture.get());
        }
    }

    private void saveForLocation(BBox bbox, List<ControlSite> controlSites) {
        try {
            var furniture = toFurniture(controlSites);
            var furnitureJson = Json.createObjectBuilder()
                    .add("bbox", Json.createArrayBuilder()
                            .add(bbox.minLat)
                            .add(bbox.minLon)
                            .add(bbox.maxLat)
                            .add(bbox.maxLon)
                            .build())
                    .add("furniture", Json.createArrayBuilder()
                            .addAll(furniture)
                            .build());
            if (Files.exists(Path.of(osmDir, "furniture.json"))) {
                var is = Files.newInputStream(Path.of(osmDir, "furniture.json"));
                var entries = Json.createReader(is).readArray();
                var builder = Json.createArrayBuilder();
                entries.forEach(builder::add);
                builder.add(furnitureJson);
                Files.write(Path.of(osmDir, "furniture.json"), builder.build().toString().getBytes());
                is.close();
            } else {
                var builder = Json.createArrayBuilder();
                builder.add(furnitureJson);
                Files.write(Path.of(osmDir, "furniture.json"), builder.build().toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonArrayBuilder toFurniture(List<ControlSite> controlSites) {
        var ret = Json.createArrayBuilder();
        controlSites.forEach(cs -> {
            var obj = Json.createObjectBuilder();
            obj.add("lat", cs.getLocation().lat);
            obj.add("lon", cs.getLocation().lon);
            obj.add("description", cs.getDescription());
            ret.add(obj);
        });
        return ret;
    }

    private Optional<List<ControlSite>> getForLocation(GHPoint location) {
        if (Files.exists(Path.of(osmDir, "furniture.json"))) {
            try {
                var is = Files.newInputStream(Path.of(osmDir, "furniture.json"));
                var entries = Json.createReader(is).readArray();
                is.close();
                for (int i = 0; i < entries.size(); i++) {
                    var f = entries.getJsonObject(i);
                    var furnitureJson = f.asJsonObject();
                    var bboxJson = furnitureJson.getJsonArray("bbox");
                    var minLat = bboxJson.getJsonNumber(0).doubleValue();
                    var minLon = bboxJson.getJsonNumber(1).doubleValue();
                    var maxLat = bboxJson.getJsonNumber(2).doubleValue();
                    var maxLon = bboxJson.getJsonNumber(3).doubleValue();
                    if (isCloseEnough(location, minLat, minLon, maxLat, maxLon)) {
                        var furniture = furnitureJson.getJsonArray("furniture").stream().map(this::parseFurniture).toList();
                        return Optional.of(furniture);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private boolean isCloseEnough(GHPoint location, double minLat, double minLon, double maxLat, double maxLon) {
        var centre = new GHPoint((maxLat + minLat) / 2.0, (maxLon + minLon) / 2.0);
        return dist(centre, location) < dist(minLat, minLon, maxLat, maxLon) / 4;  // we get 5k * 5k tiles, so this is a reasonable distance
    }

    private ControlSite parseFurniture(JsonValue jsonValue) {
        var furnitureJson = jsonValue.asJsonObject();
        var lat = furnitureJson.getJsonNumber("lat").doubleValue();
        var lon = furnitureJson.getJsonNumber("lon").doubleValue();
        var description = furnitureJson.getString("description");
        return new ControlSite(new GHPoint(lat, lon), description, ControlType.FURNITURE);
    }
}
