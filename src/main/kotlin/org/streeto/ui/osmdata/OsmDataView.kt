package org.streeto.ui.osmdata

import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class OsmDataView : View("Imported OSM Data") {
    private val controller: OsmDataController by inject()
    val model: MapDataViewModel by inject()

    override fun onBeforeShow() {
        controller.loadMaps()
    }

    override val root = vbox {
        tableview(controller.mapsList) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            prefWidth = 400.0
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            readonlyColumn("Region", MapDataModel::nameProperty) {
                prefWidth = 200.0
                cellFormat { text = it.value }

            }
            readonlyColumn("Imported", MapDataModel::dateProperty) {
                cellFormat { text = it.value.toString() }
                remainingWidth()
            }
            bindSelected(model)

            contextmenu {
                item("Delete") {
                    action {
                        confirm("Are you sure you want to delete this map?") {
                            controller.deleteMapData(model.item)
                            controller.loadMaps()
                        }
                    }
                }
                item("Update") {
                    action {
                        controller.updateMapData(model.item)
                        controller.loadMaps()
                    }
                }
            }
        }
    }
}
