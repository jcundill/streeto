package org.streeto.ui.controls

import org.streeto.ui.ControlViewModel
import tornadofx.*

class ControlDetailView : View() {
    val model: ControlViewModel by inject()

    init {
        title = "Control Details View"
    }

    override val root = vbox {
        form {
            fieldset("Control Details") {
                field("Number")
                textfield(model.number).isEditable = false

                field("Description") {
                    textfield(model.description)//.bind(descriptionProperty)
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
