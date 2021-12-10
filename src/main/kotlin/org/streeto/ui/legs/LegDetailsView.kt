package org.streeto.ui.legs

import javafx.util.StringConverter
import org.streeto.ui.Control
import org.streeto.ui.ScoredLegModel
import tornadofx.*

class LegDetailsView : View("Leg Details") {

    private val model: ScoredLegModel by inject()

    init {
        title = "Leg Details View"
    }

    class NumConverter(private val model: ScoredLegModel) : StringConverter<Control>() {
        override fun toString(p0: Control?): String {
            return p0?.number.toString()
        }

        override fun fromString(p0: String?): Control {
            return model.item.start
        }

    }

    override val root = vbox {
        form {
            fieldset("Leg Details") {
                field("Leg") {
                    textfield(model.start, converter = NumConverter(model))
                }
                field("Length") {
                    textfield(model.length)
                }
                field("Overall Score") {
                    textfield(model.overallScore)
                }
                field("Leg Length Score") {
                    textfield(model.lengthScore)
                }
                field("Leg Complexity Score") {
                    textfield(model.complexityScore)
                }
                field("Leg Route Choice Score") {
                    textfield(model.routeChoiceScore)
                }
                field("Exposes Future Controls Score") {
                    textfield(model.comesTooCloseScore)
                }
                field("Route Repetition Score") {
                    textfield(model.beeHereBeforeScore)
                }
                field("Dog Leg Score") {
                    textfield(model.dogLegScore)
                }
                field("Next Control Placement Score") {
                    textfield(model.placementScore)
                }

            }
        }
    }
}
