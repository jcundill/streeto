package org.streeto.ui.legs

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.streeto.ui.*
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

            subscribe<NextLegEvent> {
                if (controller.legList.isNotEmpty()) {
                    if(selectionModel.selectedItem != null) {
                        val idx = selectionModel.selectedIndex
                        if (idx < controller.legList.size - 1) {
                            selectionModel.select(idx + 1)
                        }
                    } else {
                        selectionModel.select(0)
                    }
                }
            }

            subscribe<PreviousLegEvent> {
                if (controller.legList.isNotEmpty()) {
                    if( selectionModel.selectedItem != null) {
                        val idx = selectionModel.selectedIndex
                        if (idx > 0) {
                            selectionModel.select(idx - 1)
                        }
                    } else {
                        selectionModel.select(0)
                    }
                    val curr = controller.legList.indexOf(model.item)
                    if (curr > 0) {
                        selectionModel.select(curr - 1)
                    }
                }
            }

            contextmenu(legViewMenu())
            readonlyColumn("Leg", ScoredLeg::endProperty) {
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