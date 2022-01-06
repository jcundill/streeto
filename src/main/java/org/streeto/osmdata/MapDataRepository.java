package org.streeto.osmdata;

import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.streeto.osmdata.OutlineUtils.*;

public class MapDataRepository {

    private final PbfFinder pbfFinder;
    private final String osmDir;
    private final List<MapData> mapDataList;
    Properties properties = new Properties();

    public MapDataRepository(String osmDir) {
        this.osmDir = osmDir;
        this.pbfFinder = new PbfFinder();
        this.mapDataList = new ArrayList<>();
        try {
            load(); // load from disk
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        mapDataList.clear();
        if (Files.exists(Path.of(osmDir, "osm.properties"))) {
            properties.load(Files.newInputStream(Path.of(osmDir, "osm.properties")));
            for (String name : properties.stringPropertyNames()) {
                var outline = properties.getProperty(name, "");
                var data = outline.split("\\|");
                var date = LocalDate.parse(data[0]);
                var ring = getOutlineFromPrefs(data[1]);
                mapDataList.add(new MapData(name, ring, date));
            }
        }
    }

    private void save() throws IOException {
        properties.store(Files.newOutputStream(Path.of(osmDir, "osm.properties")), null);
    }

    public List<MapData> getMapDataList() {
        return mapDataList;
    }

    public Optional<MapData> getMapData(@NotNull String name) {
        return mapDataList.stream().filter(mapData -> mapData.getName().equals(name)).findFirst();
    }

    public void removeMapData(@NotNull MapData mapData) {
        mapDataList.remove(mapData);
    }

    public void removeMapData(@NotNull String name) {
        mapDataList.removeIf(mapData -> mapData.getName().equals(name));
    }

    void saveMapData(MapData mapData) throws IOException {
        mapDataList.add(mapData);
        properties.put(mapData.getName(), toPrefsString(mapData));
        save();
    }

    public void updateMapData(String name) throws IOException {
        var maybeMapData = getMapData(name);
        if (maybeMapData.isPresent()) {
            var mapData = maybeMapData.get();
            var center = OutlineUtils.getCenter(mapData.getOutline());
            installMapDataFor(center);
        }
    }

    public void deleteMapData(String name) {
        var maybeMapData = mapDataList.stream().filter(m -> m.getName().equals(name)).findFirst();
        if (maybeMapData.isPresent()) {
            var mapData = maybeMapData.get();
            try {
                deleteFromDisk(mapData);
                mapDataList.remove(mapData);
                properties.remove(mapData.getName());
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteFromDisk(MapData mapData) throws IOException {
        Path dir = Path.of(osmDir, mapData.getName());
        Files.walk(dir).forEach(path -> {
            try {
                if (!Files.isDirectory(path)) {
                    Files.delete(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Files.delete(dir);
    }

    public boolean hasMapDataFor(GHPoint location) {
        for (MapData mapData : mapDataList) {
            if (isPointInsideOutline(location, mapData.getOutline())) {
                return true;
            }
        }
        return false;
    }

    public Optional<GraphHopperOSM> getMapDataFor(GHPoint location) {
        var maybeMapData = mapDataList.stream().filter(mapData -> isPointInsideOutline(location, mapData.getOutline())).findFirst();
        if (maybeMapData.isEmpty()) {
            return Optional.empty();
        } else {
            var mapData = maybeMapData.get();
            var ghWrapper = new GhWrapper();
            var gh = ghWrapper.loadGH(osmDir + "/" + mapData.getName());
            return Optional.of(gh);
        }
    }

    public Optional<GraphHopperOSM> installMapDataFor(GHPoint point) throws IOException {
        var maybePbfUrl = pbfFinder.findPbfFor(point);
        if (maybePbfUrl.isEmpty()) {
            return Optional.empty();
        } else {
            var pbfInfo = maybePbfUrl.get();
            var maybeDownloadedFile = pbfFinder.getPbfFile(pbfInfo.getUrl());
            if (maybeDownloadedFile.isEmpty()) {
                return Optional.empty();
            } else {
                var downloadedFile = maybeDownloadedFile.get();
                var name = pbfInfo.getName();
                var ghWrapper = new GhWrapper();
                ghWrapper.initGH(downloadedFile.getAbsolutePath(), osmDir + "/" + name);
                GraphHopperOSM gh = ghWrapper.loadGH(osmDir + "/" + name);
                MapData mapData = new MapData(name, pbfInfo.getOutline(), LocalDate.now());
                saveMapData(mapData);
                return Optional.of(gh);
            }
        }
    }

    @NotNull
    private String getExtractName(String pbfUrl) {
        var name = pbfUrl.substring(pbfUrl.lastIndexOf('/') + 1);
        return name.substring(0, name.indexOf('.'));
    }

    public Optional<GraphHopperOSM> loadMapDataFromPBF(@NotNull File pbfFile) throws IOException {
        var name = getExtractName(pbfFile.getName());
        var ghWrapper = new GhWrapper();
        ghWrapper.initGH(pbfFile.getAbsolutePath(), osmDir + "/" + name);
        GraphHopperOSM gh = ghWrapper.loadGH(osmDir + "/" + name);
        var bounds = gh.getGraphHopperStorage().getBounds();
        var outline = getOutlineFromBBox(bounds);
        MapData mapData = new MapData(name, outline, LocalDate.now());
        saveMapData(mapData);
        return Optional.of(gh);
    }
}
