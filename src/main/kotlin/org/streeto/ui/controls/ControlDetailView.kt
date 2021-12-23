package org.streeto.ui.controls

import org.streeto.ui.ControlViewModel
import org.streeto.ui.StreetOView
import tornadofx.*

class ControlDetailView : StreetOView("Control Details") {
    val model: ControlViewModel by inject()

    override val root = vbox {
        form {
            fieldset("Control Details") {
                field("Number")
                textfield(model.number).isEditable = false

                field("Description") {
                    textfield(model.description)
                }
                field("Location") {
                    hbox {
                        textfield(model.lat)
                        textfield(model.lon)
                    }
                }
            }
        }
    }
}
