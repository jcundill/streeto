package org.streeto.ui

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.stage.FileChooser
import org.streeto.utils.CollectionHelpers.first
import tornadofx.*
import java.io.File


class NewOSMDataView : View("New OSM Data Import") {
    private val osmData = object {
        var pbfFile = SimpleObjectProperty<File>()
        var graphDir = SimpleObjectProperty<File>()
    }

    override val root = vbox {
        form {
            fieldset {
                field("OSM PBF File") {
                    val ext = FileChooser.ExtensionFilter("extracts", "*.pbf")
                    val selected =
                        chooseFile("Choose OSM pbf extract", filters = arrayOf(ext), mode = FileChooserMode.Single)
                    if (selected.isNotEmpty()) {
                        osmData.pbfFile.value = first(selected)
                    }
                }
                field("Graph Data Directory") {
                    val selected = chooseDirectory("Graph Data Folder")
                    if (selected != null) {
                        osmData.graphDir.value = selected
                    }
                }
            }
            hbox {
                alignment = Pos.CENTER_RIGHT
                button("Save") {
                    action {
                        this@NewOSMDataView.close()
                    }
                }
                button("Cancel") {
                    action {
                        this@NewOSMDataView.close()
                    }
                }
            }
        }
    }
}
