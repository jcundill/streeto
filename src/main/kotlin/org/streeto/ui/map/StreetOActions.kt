package org.streeto.ui.map

import javafx.scene.control.Alert
import org.streeto.ui.CourseController
import org.streeto.ui.Point
import tornadofx.*

object StreetOActions {
    fun loadMapDataAction(controller: CourseController, view: View, position: Point, op: () -> Unit) {
        val haveData = controller.hasMapDataFor(position)
        if (haveData) {
            view.runAsyncWithOverlay {
                controller.loadMapDataAt(position, false)
            } ui { loaded ->
                if (loaded) {
                    op()
                }
            }
        } else {
            view.runAsyncWithOverlay {
                controller.getGeoFabrikExtractFor(position)
            } ui { extract ->
                if (extract.isPresent) {
                    confirm(
                        "No map data found for this position",
                        "Install ${extract.get().name} from 'geofabrik.de'? Depending on the area covered by this file, this could take a while."
                    ) {
                        view.runAsyncWithOverlay {
                            controller.loadMapDataAt(position, true)
                        } ui { loaded ->
                            if (loaded) {
                                op()
                            }
                        }
                    }
                } else {
                    alert(
                        Alert.AlertType.INFORMATION,
                        "No map data found for this position",
                        "No map data for this location can be installed from 'geofabrik.de'\n" +
                                "Manually download an osm.pbf format file yourself, and import it manually"
                    )
                }
            }
        }
    }
}