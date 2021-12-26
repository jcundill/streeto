package org.streeto.ui.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
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

            onMouseClicked = EventHandler {
                if (it.clickCount == 2) {
                    fire(ZoomToControlEvent(selectedItem))
                }
            }

            column("Control", Control::number) {
                contentWidth(padding = 50.0)
                isSortable = false
            }
            column("Description", Control::description) {
                isSortable = false
            }

            bindSelected(model)


            subscribe<NextLegEvent> { _ ->
                if (selectionModel.selectedIndex < controller.controlList.size - 1) {
                    selectionModel.select(selectionModel.selectedIndex + 1)
                }
            }

            subscribe<PreviousLegEvent> { _ ->
                if (selectionModel.selectedIndex > 0) {
                    selectionModel.select(selectionModel.selectedIndex - 1)
                }
            }

            subscribe<ControlSelectedEvent> { event ->
                selectionModel.select(event.control)
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