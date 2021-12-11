package org.streeto.osmdata;

import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MapDataRepository {

    private final PbfFinder pbfFinder;
    private final GhWrapper ghWrapper;
    private final String graphsDir;
    private final List<MapData> mapDataList;
    Preferences prefs = Preferences.userNodeForPackage(MapDataRepository.class);

    public MapDataRepository(String graphsDir) {
        this.graphsDir = graphsDir;
        this.mapDataList = loadMapData();
        this.pbfFinder = new PbfFinder();
        this.ghWrapper = new GhWrapper();
    }

    void saveMapData(MapData mapData) {
        mapDataList.add(mapData);
        prefs.put(mapData.getPath(), mapData.getBbox().toString());
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public Optional<GraphHopperOSM> getMapDataFrom(String graphDir) {
        if (mapDataList.stream().anyMatch(mapData -> mapData.getPath().equals(graphDir))) {
            var gh = ghWrapper.loadGH(getOsmDirectory(graphDir));
            return Optional.of(gh);
        }
        return Optional.empty();
    }

    public boolean hasMapDataFor(GHPoint point) {
        for (MapData mapData : mapDataList) {
            if (mapData.getBbox().contains(point.lat, point.lon)) {
                return true;
            }
        }
        return false;
    }

    public Optional<GraphHopperOSM> getMapDataFor(GHPoint point) {
        var maybeMapData = mapDataList.stream().filter(mapData -> mapData.getBbox().contains(point.lat, point.lon)).findFirst();
        if (maybeMapData.isEmpty()) {
            return Optional.empty();
        } else {
            var mapData = maybeMapData.get();
            var gh = ghWrapper.loadGH(getOsmDirectory(mapData.getPath()));
            return Optional.of(gh);
        }
    }

    public Optional<GraphHopperOSM> installMapDataFor(GHPoint point, String graphDir) {
        var osmDir = getOsmDirectory(graphDir);
        var maybePbfUrl = pbfFinder.findPbfFor(point);
        if (maybePbfUrl.isEmpty()) {
            return Optional.empty();
        } else {
            var pbfUrl = maybePbfUrl.get();
            var maybeDownloadedFile = pbfFinder.getPbfFile(pbfUrl);
            if (maybeDownloadedFile.isEmpty()) {
                return Optional.empty();
            } else {
                var downloadedFile = maybeDownloadedFile.get();
                ghWrapper.initGH(downloadedFile.getAbsolutePath(), osmDir);
                GraphHopperOSM gh = ghWrapper.loadGH(osmDir);
                BBox bbox = gh.getGraphHopperStorage().getBounds();
                MapData mapData = new MapData(graphDir, bbox);
                saveMapData(mapData);
                return Optional.of(gh);
            }
        }
    }

    @NotNull
    private String getOsmDirectory(String graphDir) {
        return graphsDir + "/" + graphDir;
    }

    List<MapData> loadMapData() {
        List<MapData> maps = new ArrayList<>();
        try {
            var data = prefs.keys();
            for (String name : data) {
                var bounds = prefs.get(name, "");
                var box = bounds.split(",");
                var bbox = new BBox(Double.parseDouble(box[0]), Double.parseDouble(box[1]), Double.parseDouble(box[2]), Double.parseDouble(box[3]));
                maps.add(new MapData(name, bbox));
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return maps;
    }


}
