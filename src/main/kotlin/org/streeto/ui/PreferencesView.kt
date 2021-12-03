package org.streeto.ui

import javafx.geometry.Pos
import javafx.scene.control.TabPane
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import tornadofx.*

class PreferencesView : View("StreetO Preferences") {
    val controller: CourseController by inject()
    val model: PreferencesViewModel by inject()//handler.loadPreferences()

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
                        field("Max Shared on Route Choice") {
                            textfield(model.maxRouteShare)
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
                    }
                }
            }
            tab("Course Scorers") {
                form {
                    fieldset {
                        fieldset {
                            field("Dog Leg Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.dogLegWeighting)
                                }
                            }
                            field("Leg Complexity Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.legComplexityWeighting)
                                }
                            }
                            field("Leg Length Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.legLengthWeighting)
                                }
                            }
                            field("Leg Route Choice Weighting") {
                                 slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.routeChoiceWeighting)
                                }
                            }
                            field("Avoid Route Repetition Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.beenHereBeforeWeighting)
                                }
                            }
                            field("Avoid Future Controls Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.comesTooCloseWeighting)
                                }
                            }
                            field("Control Site Placement Weighting") {
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.distinctControlSiteWeighting)
                                }
                            }
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
                                checkbox { model.printA3OnA4 }
                            }
                            field("Split Map for Better Scale") {
                                 checkbox { model.splitForBetterScale }
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
                    model.rollback()
                    controller.flushPreferences(model.item)
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
}
