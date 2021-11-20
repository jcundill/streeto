package org.streeto.ui

import com.sothawo.mapjfx.*
import com.sothawo.mapjfx.event.MapViewEvent
import javafx.scene.layout.Priority
import tornadofx.*

class MapFXMapView : View("Course") {

    val wmsParams = WMSParam()
        .setUrl("http://ows.terrestris.de/osm/service")
        .addParam("layers", "OSM-WMS")

    val xyzParams = XYZParam()
        .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
        .withAttributions("'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'")


    override val root = vbox {
        jfxMap {
            vgrow = Priority.ALWAYS
            useMaxSize = true
            mapType = MapType.XYZ
            initializedProperty().addListener(ChangeListener { _, _, newValue ->
                run {
                    if (newValue) {
                        // now initialize the mapView
                        center = Coordinate(49.015511, 8.323497)
                        animationDuration = 500
                        setWMSParam(wmsParams)
                        setXYZParam(xyzParams)

                    }
                }
            })
            addEventHandler(MapViewEvent.MAP_CLICKED) {
                println(it.coordinate)
            }
            addEventHandler(MapViewEvent.MAP_RIGHTCLICKED) {

            }
            initialize(Configuration.builder().build())
        }
    }
}


