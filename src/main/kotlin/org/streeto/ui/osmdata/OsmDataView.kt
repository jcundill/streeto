package org.streeto.ui.osmdata

import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import org.streeto.ui.StreetOView
import tornadofx.*

class OsmDataView : StreetOView("Imported OSM Data") {
    private val controller: OsmDataController by inject()
    val model: MapDataViewModel by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
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
        hbox {
            paddingAll = 10.0
            alignment = Pos.CENTER
            button("Import From PBF File ...") {
                action {
                    val pbf = FileChooser.ExtensionFilter("PBF", "*.pbf")
                    val file = chooseFile("Open File", filters = arrayOf(pbf), mode = FileChooserMode.Single)
                    file.map { pbfFile ->
                        this@OsmDataView.runAsyncWithOverlay {
                            controller.loadMapDataFromPBF(pbfFile)
                        } ui { loaded ->
                            if (loaded) {
                                controller.loadMaps()
                            } else {
                                alert(Alert.AlertType.ERROR, "Error", "Map data could not be loaded")
                            }
                        }
                    }

                }
            }
        }

    }
}
