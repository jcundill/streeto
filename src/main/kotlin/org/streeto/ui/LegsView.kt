package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority
import tornadofx.*

class LegsView : View("Legs") {
    private val controller: CourseController by inject()
    private val model: ScoredLegModel by inject()

    override val root = vbox {
        tableview(controller.legList) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            readonlyColumn("Leg", ScoredLeg::startProperty).cellFormat {
                text = it.value.number
            }
            readonlyColumn("Score", ScoredLeg::overallScoreProperty)
                .remainingWidth()
                .cellFormat {
                    text = it.value.toString()
                }

            onSelectionChange {
                println("selection -> ${it?.overallScoreProperty?.value}")
            }
            contextmenu {
                item("Details") {
                    action {
                        workspace.openInternalWindow<LegDetailsView>(modal = false)
                    }
                }
                item("Zoom to Leg") {
                    action {
                        if (selectedItem != null) {
                            controller.selectLegFrom(selectedItem?.start)
                        }
                    }
                }
            }
            bindSelected(model)
        }
    }
    override val closeable = SimpleBooleanProperty(false)
}
