package org.streeto.ui

import javafx.geometry.Pos
import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import tornadofx.*

class PreferencesView : View("StreetO Preferences") {
    val controller: CourseController by inject()
    val model: PreferencesViewModel by inject()//handler.loadPreferences()

    override val root = vbox {
        form {
            tabpane {
                tab("Leg Length Constraints") {
                    fieldset {
                        field {
                            label("Desired Average Leg Length")
                            textfield(model.avgLegDistance)
                        }
                        field {
                            label("Min Leg Distance")
                            textfield(model.minLegDistance)
                        }
                        field {
                            label("Max Leg Distance")
                            textfield(model.maxLegDistance)
                        }
                        field {
                            label("Nearest Approach to Finish")
                            textfield(model.minApproachToFinish)
                        }
                        field {
                            label("Max Allowed First Leg Length")
                            textfield(model.maxFirstLegLength)
                        }
                        field {
                            label("Max Allowed Last Leg Length")
                            textfield(model.maxLastLegLength)
                        }
                        field {
                            label("Allowed Course Length Delta Factor")
                            textfield(model.allowedCourseLengthDelta)
                        }
                        field {
                            label("Max Shared on Route Choice")
                            textfield(model.maxRouteShare)
                        }
                    }
                }
                tab("Control Placement") {
                    fieldset {
                        fieldset("Control Separation Factors") {
                            field {
                                label("Min Control Separation")
                                textfield(model.minControlSeparation)
                            }
                        }
                        fieldset("Junction Score Factors") {
                            field {
                                label("Min Average Distance between Junctions")
                                textfield(model.turnDensity)
                            }
                            field {
                                label("Junction Control Placement Factor")
                                textfield(model.junctionScoreFactor)
                            }
                        }
                        fieldset("Bend Score Factors") {
                            field {
                                label("Bend Control Placement Factor")
                                textfield(model.bendScoreFactor)
                            }
                            field {
                                label("Min Bend Angle")
                                textfield(model.minTurnAngle)
                            }
                        }
                        fieldset("Street Furniture Factors") {
                            field {
                                label("Street Furniture Distance")
                                textfield(model.maxFurnitureDistance)
                            }
                        }
                    }
                }
                tab("Course Scorers") {
                    fieldset {
                        fieldset {
                            field {
                                label("Dog Leg Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.dogLegWeighting)
                                }
                            }
                            field {
                                label("Leg Complexity Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.legComplexityWeighting)
                                }
                            }
                            field {
                                label("Leg Length Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.legLengthWeighting)
                                }
                            }
                            field {
                                label("Leg Route Choice Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.routeChoiceWeighting)
                                }
                            }
                            field {
                                label("Avoid Route Repetition Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.beenHereBeforeWeighting)
                                }
                            }
                            field {
                                label("Avoid Future Controls Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.comesTooCloseWeighting)
                                }
                            }
                            field {
                                label("Control Site Placement Weighting")
                                slider {
                                    min = 0.0
                                    max = 1.0
                                    bind(model.distinctControlSiteWeighting)
                                }
                            }
                        }
                    }
                }
                tab("Mapping Options") {
                    fieldset {
                        fieldset("Map Type") {
                            field {
                                label("Map Appearance")
                                combobox(model.mapStyle, MapStyle.values().asList())
                            }
                            field {
                                label("Maximum Scale Map")
                                combobox(model.maxMapScale, listOf(5000.0, 7500.0, 10000.0, 12500.0, 15000.0))
                            }
                        }
                        fieldset("Map Printing Options") {
                            field {
                                label("Paper Size")
                                combobox(model.paperSize, PaperSize.values().asList())
                            }
                            field {
                                label("Print A3 Maps on A4 Paper")
                                checkbox { model.printA3OnA4 }
                            }
                            field {
                                label("Split Map for Better Scale")
                                checkbox { model.splitForBetterScale }
                            }
                        }
                    }
                }
                tab("Course Evolution") {
                    fieldset {
                        fieldset("Stopping Criteria") {
                            field {
                                label("Max Execution Time (s)")
                                textfield(model.maxExecutionTime)
                            }
                            field {
                                label("Number of Generations")
                                textfield(model.maxGenerations)
                            }
                            field {
                                label("Fitness Stop level")
                                textfield(model.stoppingFitness)
                            }
                        }
                        fieldset("GA Parameters") {
                            field {
                                label("Swap Probability")
                                textfield(model.controlSwapProbability)
                            }
                            field {
                                label("Mutation Probability")
                                textfield(model.mutateProbability)
                            }
                            field {
                                label("Max Phenotype Age")
                                textfield(model.maxPhenotypeAge)
                            }
                            field {
                                label("Offspring Fraction")
                                textfield(model.offspringFraction)
                            }
                            field {
                                label("Population Size")
                                textfield(model.populationSize)
                            }
                        }
                        fieldset("Control Replacement Factors") {
                            field {
                                label("Allowed Mutation Radius")
                                textfield(model.mutationRadius)
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
}
