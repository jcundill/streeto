package org.streeto.osmdata;

import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Envelope;

import javax.json.Json;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class PbfFinder {

    public static final String GEOFABRIK_DE_INDEX_V_1_JSON = "https://download.geofabrik.de/index-v1.json";

    public Optional<String> findPbfFor(GHPoint location) {

        String pbfUrl = null;
        try {
            var url = new URL(GEOFABRIK_DE_INDEX_V_1_JSON);
            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.connect();
            var buff = new BufferedInputStream(connection.getInputStream());

            var json = Json.createReader(buff).readObject();
            var features = json.getJsonArray("features");
            var smallestWidth = Double.MAX_VALUE;
            var smallestHeight = Double.MAX_VALUE;
            for (int i = 0; i < features.size(); i++) {
                var feature = features.getJsonObject(i);
                var properties = feature.getJsonObject("properties");
                var geometry = feature.getJsonObject("geometry");
                var coordinates = geometry.getJsonArray("coordinates");
                var envelope = new Envelope();
                var polygon = coordinates.getJsonArray(0).getJsonArray(0);
                for (int j = 0; j < polygon.size(); j++) {
                    var point = polygon.getJsonArray(j);
                    var lat = point.getJsonNumber(1).doubleValue();
                    var lon = point.getJsonNumber(0).doubleValue();
                    envelope.expandToInclude(lon, lat);
                }
                if (envelope.contains(location.getLon(), location.getLat())) {
                    var width = envelope.getWidth();
                    var height = envelope.getHeight();
                    if (width < smallestWidth && height < smallestHeight) {
                        pbfUrl = properties.getJsonObject("urls").getJsonString("pbf").getString();
                        smallestWidth = width;
                        smallestHeight = height;
                    }
                }
            }

        } catch (IOException e) {
            // can't find the pbf, return empty
        }

        return Optional.ofNullable(pbfUrl);
    }

    public Optional<File> getPbfFile(String pbfUrl) {
        try {
            File pbfFile = File.createTempFile("pbf", ".pbf");
            var in = new URL(pbfUrl).openStream();
            var buff = new BufferedInputStream(in);
            Files.copy(buff, Paths.get(pbfFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(pbfFile);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
