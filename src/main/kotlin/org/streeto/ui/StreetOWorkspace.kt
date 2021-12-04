package org.streeto.ui

import javafx.application.Platform
import javafx.stage.FileChooser
import tornadofx.*
import java.util.*

class StreetOWorkspace : Workspace("Editor", NavigationMode.Tabs) {

    private val courseController: CourseController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
        dock<OpenLayersMapView>()
    }

    override fun onDock() {
        super.onDock()
        leftDrawer.item(find(ControlsView::class))
        rightDrawer.item(find(LegsView::class))
    }

    init {
        menubar {
            isUseSystemMenuBar = true
            menu("File") {
                menu("New") {
                    item("OSM Data") {
                        action {
                            find<NewOSMDataView>().openModal()
                        }
                    }
                    item("Course").action {
                        find<NewCourseView>().openModal()
                    }
                    item("From Properties") {
                        action {
                            val ext = FileChooser.ExtensionFilter("properties", "*.properties")
                            val propsFiles =
                                chooseFile("Open File", filters = arrayOf(ext), mode = FileChooserMode.Single)
                            if (propsFiles.isNotEmpty()) {
                                with(propsFiles[0]) {
                                    runAsyncWithOverlay {
                                        try {
                                            val props = Properties()
                                            props.load(inputStream())
                                            //courseController.initializeGH(props)
                                        } catch (e: Exception) {
                                            error(header = "Invalid File", content = e.message)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item("Open").action {
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFile = chooseFile("Open File", filters = arrayOf(kml, gpx), mode = FileChooserMode.Single)
                    courseFile.map {
                        courseController.loadCourse(it)
                    }
                }
                separator()
                item("Save").action {

                }
                item("Save As").action {
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFiles = chooseFile("Open File", filters = arrayOf(kml, gpx), mode = FileChooserMode.Save)
                    if (courseFiles.isNotEmpty()) {
                        courseController.saveAs(courseFiles[0])
                    }

                }
                separator()
                item("Create MapRun Files")
                item("Create Map PDF")
                separator()
                item("Quit", "Shortcut+Q") {
                }.action {
                    Platform.exit()
                }
            }
            menu("Course") {
                item("Create From Controls") {
                    action {
                        courseController.sniffer.reset()
                        find<GenerationProgressView> {
                            closeableWhen { courseController.sniffer.completedProperty }
                            openModal()
                        }
                        courseController.generateFromControls()
                    }
                }
                item("Score Controls") {
                    action {
                        courseController.scoreControls()
                    }
                }
            }
            menu("Map") {
                item("Reset Rotation").action {
                    fire(ResetRotationEvent)
                }
                item("Zoom To Fit Course").action {
                    fire(ZoomToFitCourseEvent)
                }
                item("Zoom To Current Leg").action {
                    fire(ZoomToFitLegEvent)
                }
            }
            menu("Show") {
                checkmenuitem("Route") {
                    selectedProperty().onChange {
                        fire(RouteVisibilityEvent(it))
                    }
                }
                checkmenuitem("Route Choice") {
                    selectedProperty().onChange {
                        fire(RouteChoiceVisibilityEvent(it))
                    }
                }
                separator()
                item("Preferences") {
                    action {
                        workspace.openInternalWindow<PreferencesView>()
                    }
                }
            }
            menu("Help") {
                item("About").action {
                    workspace.openInternalWindow<AboutView>()
                }
            }
        }
    }
}
