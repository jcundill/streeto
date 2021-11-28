package org.streeto.ui

import org.streeto.mapping.MapStyle
import org.streeto.mapping.PaperSize
import tornadofx.*

class PreferencesView : View("StreetO Preferences") {
    val handler = PreferencesHandler()
    val prefs = handler.loadPreferences()
    override val root = vbox {
        form {
            tabpane {
                tab("Course Constraints") {
                    fieldset {
                        field {
                            label("Min Leg Distance")
                            textfield(prefs.minLegDistanceProperty)
                        }
                        field {
                            label("Max Leg Distance")
                            textfield(prefs.maxLegDistanceProperty)
                        }
                        field {
                            label("Nearest Approach to Finish")
                            textfield(prefs.minApproachToFinishProperty)
                        }
                        field {
                            label("Max Allowed First Leg Length")
                            textfield(prefs.maxFirstControlDistanceProperty)
                        }
                        field {
                            label("Allowed Course Length Delta Factor")
                            textfield(prefs.allowedCourseLengthDeltaProperty)
                        }
                        field {
                            label("Allowed Mutation Radius")
                            textfield(prefs.mutationRadiusProperty)
                        }
                        field {
                            label("Max Shared on Route Choice")
                            textfield(prefs.maxRouteShareProperty)
                        }
                        field {
                            label("Street Furniture Distance")
                            textfield(prefs.maxFurnitureDistanceProperty)
                        }
                        field {
                            label("Min Turn Angle")
                            textfield(prefs.minTurnAngleProperty)
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
                                bind(prefs.dogLegWeightingProperty)
                            }
                        }
                        field {
                            label("Leg Complexity Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.legComplexityWeightingProperty)
                            }
                        }
                        field {
                            label("Leg Length Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.legLengthWeightingProperty)
                            }
                        }
                        field {
                            label("Leg Route Choice Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.routeChoiceWeightingProperty)
                            }
                        }
                        field {
                            label("Avoid Route Repetition Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.beenHereBeforeWeightingProperty)
                            }
                        }
                        field {
                            label("Avoid Future Controls Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.comesTooCloseWeightingProperty)
                            }
                        }
                        field {
                            label("Control Site Placement Weighting")
                            slider {
                                min = 0.0
                                max = 1.0
                                bind(prefs.distinctControlSiteWeightingProperty)
                            }
                        }
                    }

                }
                tab("Mapping Options") {
                    fieldset {
                        field {
                            label("Map Appearance")
                            combobox(prefs.mapStyleProperty, MapStyle.values().asList())
                        }
                        field {
                            label("Split Map for Better Scale")
                            checkbox { prefs.splitForBetterScaleProperty }
                        }
                        field {
                            label("Paper Size")
                            combobox(prefs.paperSizeProperty, PaperSize.values().asList())
                        }
                        field {
                            label("Print A3 Maps on A4 Paper")
                            checkbox { prefs.printA3OnA4Property }
                        }
                        field {
                            label("Maximum Scale Map")
                            combobox(prefs.maxMapScaleProperty, listOf(5000.0, 7500.0, 10000.0, 12500.0, 15000.0))
                        }
                    }
                }
                tab("Course Evolution") {
                    fieldset {
                        field {
                            label("Max Execution Time (s)")
                            textfield(prefs.maxExecutionTimeProperty)
                        }
                        field {
                            label("Number of Generations")
                            textfield(prefs.maxGenerationsProperty)
                        }
                        field {
                            label("Fitness Stop level")
                            textfield(prefs.stoppingFitnessProperty)
                        }
                        field {
                            label("Swap Probability")
                            textfield(prefs.controlSwapProbabilityProperty)
                        }
                        field {
                            label("Mutation Probability")
                            textfield(prefs.mutateProbabilityProperty)
                        }
                        field {
                            label("Max Phenotype Age")
                            textfield(prefs.maxPhenotypeAgeProperty)
                        }
                        field {
                            label("Offspring Fraction")
                            textfield(prefs.offspringFractionProperty)
                        }
                        field {
                            label("Population Size")
                            textfield(prefs.populationSizeProperty)
                        }

                    }
                }
            }
            button("Commit") {
                action {
                    println(prefs.minLegDistanceProperty.value)
                    println(prefs.dogLegWeightingProperty.value)
                    println(prefs.minLegDistance)
                    println(prefs.dogLegWeighting)
                    handler.flushPreferences(prefs)
                }
            }
        }
    }
}
