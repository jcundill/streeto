package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority
import tornadofx.*

class ControlsView : View("Controls") {
    private val controller: CourseController by inject()

    override val root = vbox {
        tableview(controller.getControls()) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            readonlyColumn("Control", Control::number).contentWidth(padding = 50.0)
            readonlyColumn("Description", Control::description)

//            onSelectionChange {
//                println("selection -> $it")
//                controller.setSelected(it)
//            }
            contextmenu {
                item("Zoom To Control") {
                    action {
                        controller.selectControl(selectedItem)
                    }
                }

                item("Zoom to Leg Before") {
                    //disableWhen{ SimpleBooleanProperty(selectedItem == null || selectedItem!!.number == "S1")}
                    action {
                        controller.selectLegTo(selectedItem)
                    }
                }
                item("Zoom to Leg After") {
                    //disableWhen{ SimpleBooleanProperty(selectedItem == null || selectedItem!!.number == "F1")}
                    action {
                        controller.selectLegFrom(selectedItem)
                    }
                }
            }
            //smartResize()
        }
    }
    override val closeable = SimpleBooleanProperty(false)
}