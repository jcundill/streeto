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
                tab("Course Constraints" ) {
                    fieldset {
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
                            label("Min Control Separation")
                            textfield(model.minControlSeparation)
                        }
                        field {
                            label("Min Average Distance between Junctions")
                            textfield(model.turnDensity)
                        }
                        field {
                            label("Allowed Course Length Delta Factor")
                            textfield(model.allowedCourseLengthDelta)
                        }
                        field {
                            label("Junction Control Placement Factor")
                            textfield(model.junctionScoreFactor)
                        }
                        field {
                            label("Bend Control Placement Factor")
                            textfield(model.bendScoreFactor)
                        }
                        field {
                            label("Max Shared on Route Choice")
                            textfield(model.maxRouteShare)
                        }
                        field {
                            label("Street Furniture Distance")
                            textfield(model.maxFurnitureDistance)
                        }
                        field {
                            label("Min Turn Angle")
                            textfield(model.minTurnAngle)
                        }
                    }
                }
                tab("Course Scorers") {
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
                tab("Mapping Options") {
                    fieldset {
                        field {
                            label("Map Appearance")
                            combobox(model.mapStyle, MapStyle.values().asList())
                        }
                        field {
                            label("Split Map for Better Scale")
                            checkbox { model.splitForBetterScale }
                        }
                        field {
                            label("Paper Size")
                            combobox(model.paperSize, PaperSize.values().asList())
                        }
                        field {
                            label("Print A3 Maps on A4 Paper")
                            checkbox { model.printA3OnA4 }
                        }
                        field {
                            label("Maximum Scale Map")
                            combobox(model.maxMapScale, listOf(5000.0, 7500.0, 10000.0, 12500.0, 15000.0))
                        }
                    }
                }
                tab("Course Evolution") {
                    fieldset {
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
                        field {
                            label("Allowed Mutation Radius")
                            textfield(model.mutationRadius)
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
