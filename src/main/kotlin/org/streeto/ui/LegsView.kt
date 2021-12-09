package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class LegsView : View("Legs") {
    private val controller: CourseController by inject()
    private val model: ScoredLegModel by inject()

    override val root = vbox {
        tableview(controller.legList) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            bindSelected(model)

            contextmenu(legViewMenu())
            readonlyColumn("Leg", ScoredLeg::startProperty) {
                cellFormat {
                    text = it.value.number
                }
                isSortable = false
            }
            readonlyColumn("Score", ScoredLeg::overallScoreProperty) {
                remainingWidth()
                cellFormat {
                    text = it.value.toString()
                }
                isSortable = false
            }
        }
    }

    private fun TableView<ScoredLeg>.legViewMenu(): ContextMenu.() -> Unit =
        {
            item("Details") {
                action {
                    workspace.openInternalWindow<LegDetailsView>(modal = false)
                }
            }
            item("Zoom to Leg") {
                action {
                    if (selectedItem != null) {
                        controller.selectLegFrom(selectedItem?.start)
                        fire(ZoomToFitLegEvent)
                    }
                }
            }
        }

    override val closeable = SimpleBooleanProperty(false)
}
