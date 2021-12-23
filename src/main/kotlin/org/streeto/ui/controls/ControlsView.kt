package org.streeto.ui.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority
import org.streeto.ui.*
import tornadofx.*

class ControlsView : StreetOView("Controls") {
    private val controller: CourseController by inject()
    val model: ControlViewModel by inject()

    override val root = vbox {
        tableview(controller.controlList) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            column("Control", Control::number) {
                contentWidth(padding = 50.0)
                isSortable = false
            }
            column("Description", Control::description) {
                isSortable = false
            }

            bindSelected(model)

            onSelectionChange {
                println("selection -> $it")
                println(model.item)
                println("${model.number}, ${model.description}, ${model.lat}, ${model.lon}")
            }

            contextmenu {
                item("Details") {
                    action {
                        workspace.openInternalWindow<ControlDetailView>(modal = false)
                    }
                }
                item("Zoom To Control") {
                    action {
                        fire(ZoomToControlEvent(selectedItem))
                    }
                }

                item("Zoom to Leg Before") {
                    action {
                        controller.selectLegTo(selectedItem)
                        fire(ZoomToFitLegEvent)
                    }
                }
                item("Zoom to Leg After") {
                    action {
                        controller.selectLegFrom(selectedItem)
                        fire(ZoomToFitLegEvent)
                    }
                }
            }
        }
    }
    override val closeable = SimpleBooleanProperty(false)
}