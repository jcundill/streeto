package org.streeto.ui.preferences

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import org.streeto.ui.CourseController
import org.streeto.ui.StreetOView
import tornadofx.*

class PreferencesView : StreetOView("StreetO Preferences") {
    val controller: CourseController by inject()
    val model: PreferencesViewModel by inject()

    override val root = vbox {
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("Leg Length Constraints") {
                form {
                    fieldset {
                        field("Desired Average Leg Length") {
                            textfield(model.avgLegDistance)
                        }
                        field("Min Leg Distance") {
                            textfield(model.minLegDistance)
                        }
                        field("Max Leg Distance") {
                            textfield(model.maxLegDistance)
                        }
                        field("Nearest Approach to Finish") {
                            textfield(model.minApproachToFinish)
                        }
                        field("Max Allowed First Leg Length") {
                            textfield(model.maxFirstLegLength)
                        }
                        field("Max Allowed Last Leg Length") {
                            textfield(model.maxLastLegLength)
                        }
                        field("Allowed Course Length Delta") {
                            textfield(model.allowedCourseLengthDelta)
                        }
                    }
                }
            }
            tab("Control Placement") {
                form {
                    fieldset {
                        fieldset("Control Separation Factors") {
                            field("Min Control Separation") {
                                textfield(model.minControlSeparation)
                            }
                        }
                        fieldset("Junction Score Factors") {
                            field("Min Average Distance between Junctions") {
                                textfield(model.turnDensity)
                            }
                            field("Junction Control Placement Score") {
                                textfield(model.junctionScoreFactor)
                            }
                        }
                        fieldset("Bend Score Factors") {
                            field("Min Bend Angle") {
                                textfield(model.minTurnAngle)
                            }
                            field("Bend Control Placement Score") {
                                textfield(model.bendScoreFactor)
                            }
                        }
                        fieldset("Street Furniture Factors") {
                            field("Street Furniture Distance") {
                                textfield(model.maxFurnitureDistance)
                            }
                        }
                        fieldset("Leg Similarity Factors") {
                            field("Max Shared on Route Choice") {
                                textfield(model.maxRouteShare)
                            }
                            field("Route Similarity Cell Size") {
                                textfield(model.csimCellSize)
                            }
                        }
                    }
                }
            }
            tab("Course Scorers") {
                form {
                    fieldset {
                        fieldset {
                            add(scorerSlider("Dog Leg Weighting", model.dogLegWeighting))
                            add(scorerSlider("Leg Complexity Weighting", model.legComplexityWeighting))
                            add(scorerSlider("Leg Length Weighting", model.legLengthWeighting))
                            add(scorerSlider("Leg Route Choice Weighting", model.routeChoiceWeighting))
                            add(scorerSlider("Avoid Route Repetition Weighting", model.beenHereBeforeWeighting))
                            add(scorerSlider("Avoid Future Controls Weighting", model.comesTooCloseWeighting))
                            add(scorerSlider("Control Site Placement Weighting", model.distinctControlSiteWeighting))
                        }
                    }
                }
            }
            tab("Mapping Options") {
                form {
                    fieldset {
                        fieldset("Map Type") {
                            field("Map Appearance") {
                                combobox(model.mapStyle, MapStyle.values().asList())
                            }
                            field("Maximum Map Scale") {
                                combobox(model.maxMapScale, listOf(5000.0, 7500.0, 10000.0, 12500.0, 15000.0))
                            }
                        }
                        fieldset("Map Printing Options") {
                            field("Paper Size") {
                                combobox(model.paperSize, PaperSize.values().asList())
                            }
                            field("Print A3 Maps on A4 Paper") {
                                checkbox {
                                    isSelected = model.printA3OnA4.value
                                    enableWhen { model.paperSize.isEqualTo(PaperSize.A3) }
                                    selectedProperty().addListener { _, _, newValue ->
                                        model.printA3OnA4.value = newValue
                                    }
                                }
                            }
                            field("Split Map for Better Scale") {
                                checkbox {
                                    isSelected = model.splitForBetterScale.value
                                    selectedProperty().addListener { _, _, newValue ->
                                        model.splitForBetterScale.value = newValue
                                    }
                                }
                            }
                        }
                    }
                }
            }
            tab("Course Evolution") {
                form {
                    fieldset {
                        fieldset("Stopping Criteria") {
                            field("Max Execution Time (s)") {
                                textfield(model.maxExecutionTime)
                            }
                            field("Max Number of Generations") {
                                textfield(model.maxGenerations)
                            }
                            field("Fitness Stop level") {
                                textfield(model.stoppingFitness)
                            }
                        }
                        fieldset("GA Parameters") {
                            field("Swap Probability") {
                                textfield(model.controlSwapProbability)
                            }
                            field("Mutation Probability") {
                                textfield(model.mutateProbability)
                            }
                            field("Max Phenotype Age") {
                                textfield(model.maxPhenotypeAge)
                            }
                            field("Offspring Fraction") {
                                textfield(model.offspringFraction)
                            }
                            field("Population Size") {
                                textfield(model.populationSize)
                            }
                        }
                        fieldset("Control Replacement Factors") {
                            field("Allowed Mutation Radius") {
                                textfield(model.mutationRadius)
                            }
                        }
                    }
                }
            }
        }
        hbox {
            paddingAll = 10.0
            alignment = Pos.CENTER_RIGHT
            button("Save") {
                action {
                    model.commit()
                    controller.flushPreferences(model.item)
                    close()
                }
            }
            button("Reset") {
                action {
                    controller.resetPreferences()
                }
            }
            button("Cancel") {
                action {
                    model.rollback()
                    close()
                }
            }
        }
    }

    private fun scorerSlider(label: String, property: SimpleDoubleProperty): Field {
        return field(label) {
            slider {
                min = 0.0
                max = 1.0
                majorTickUnit = 0.1
                isShowTickMarks = true
                isShowTickLabels = true
                bind(property)
            }
        }
    }
}
