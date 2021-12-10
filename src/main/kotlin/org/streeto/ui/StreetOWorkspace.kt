package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.MenuBar
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import org.streeto.ui.controls.ControlsView
import org.streeto.ui.coursedetails.CourseDetailsView
import org.streeto.ui.evolution.GenerationProgressView
import org.streeto.ui.legs.LegsView
import org.streeto.ui.map.OpenLayersMapView
import org.streeto.ui.preferences.PreferencesView
import tornadofx.*
import java.util.*

class StreetOWorkspace : Workspace("Editor", NavigationMode.Tabs) {

    private val courseController: CourseController by inject()
    private val mapView: OpenLayersMapView by inject()

    private val haveControls = SimpleBooleanProperty(false)

    init {
        courseController.controlList.onChange { newValue ->
            haveControls.value = newValue.list.size > 0

        }
    }



    override fun onBeforeShow() {
        super.onBeforeShow()
        dock<OpenLayersMapView>()
        workspace.tabContainer.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
    }

    override fun onDock() {
        super.onDock()
        leftDrawer.item(find(CourseDetailsView::class))
        leftDrawer.item(find(ControlsView::class))
        rightDrawer.item(find(LegsView::class))
    }

    init {
        menubar {
            isUseSystemMenuBar = true
            fileMenu()
            courseMenu()
            mapMenu()
            showMenu()
            helpMenu()
        }
    }

    private fun MenuBar.helpMenu() {
        menu("Help") {
            item("About").action {
                workspace.openInternalWindow<AboutView>()
            }
        }
    }

    private fun MenuBar.fileMenu() {
        menu("File") {
            menu("New") {
                item("OSM Data") {
                    action {
                        find<NewOSMDataView>().openModal()
                    }
                }
                item("Course") {
                    enableWhen(courseController.isReady)
                    action {
                        find<NewCourseView>().openModal()
                        courseController.removeAllControls()
                        fire(CourseUpdatedEvent)
                    }
                }
                item("From Properties") {
                    enableWhen(courseController.isReady)
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
                                    } catch (e: Exception) {
                                        error(header = "Invalid File", content = e.message)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item("Open") {
                enableWhen(courseController.isReady)
                action {
                    val all = FileChooser.ExtensionFilter("Any", "*.*")
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFile =
                        chooseFile("Open File", filters = arrayOf(all, kml, gpx), mode = FileChooserMode.Single)
                    courseFile.map {
                        mapView.runAsyncWithOverlay {
                            courseController.loadCourse(it)
                        } ui {
                            fire(CourseUpdatedEvent)
                            fire(ZoomToFitCourseEvent)
                        }
                    }
                }
            }

            separator()
            item("Save") {

            }
            item("Save As") {
                action {
                    val all = FileChooser.ExtensionFilter("Any", "*.*")
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFiles =
                        chooseFile("Open File", filters = arrayOf(all, kml, gpx), mode = FileChooserMode.Save)
                    if (courseFiles.isNotEmpty()) {
                        courseController.saveAs(courseFiles[0])
                    }
                }
            }
            separator()
            item("Create MapRun Files") {
                action {
                    val directory = chooseDirectory(title = "KML+KMZ Save Location")
                    if (directory != null) {
                        mapView.runAsyncWithOverlay {
                            courseController.generateMapRunFiles(directory)
                        }
                    }
                }
            }
            item("Create Map PDF") {
                action {
                    val directory = chooseDirectory(title = "PDF Save Location")
                    if (directory != null) {
                        mapView.runAsyncWithOverlay {
                            courseController.generatePDF(directory)
                        }
                    }
                }
            }
            separator()
            item("Quit", "Shortcut+Q") {
                action {
                    Platform.exit()
                }
            }
        }
    }

    private fun MenuBar.showMenu() {
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
    }

    private fun MenuBar.mapMenu() {
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
    }

    private fun MenuBar.courseMenu() {
        menu("Course") {
            item("Clear Numbered Controls") {
                enableWhen(haveControls)
                action {
                    courseController.removeNumberedControls()
                    fire(CourseUpdatedEvent)
                }
            }
            item("Clear All") {
                enableWhen(haveControls)
                action {
                    courseController.removeAllControls()
                    fire(CourseUpdatedEvent)
                }
            }
            separator()
            item("Improve Existing Controls") {
                enableWhen(haveControls)
                action {
                    if (courseController.controlList.size > 2) {//need at least one numbered control
                        courseController.sniffer.reset()
                        find<GenerationProgressView> {
                            model.finished.value = false
                            closeableWhen { courseController.sniffer.completedProperty }
                            openModal()
                        }
                        runAsync {
                            courseController.generateFromControls()
                        } ui {
                            fire(CourseUpdatedEvent)
                            fire(ZoomToFitCourseEvent)
                        }
                    }
                }
            }
            item("Seed from Existing Controls") {
                enableWhen(haveControls)
                action {
                    courseController.sniffer.reset()
                    find<GenerationProgressView> {
                        model.finished.value = false
                        closeableWhen { courseController.sniffer.completedProperty }
                        openModal()
                    }
                    runAsync {
                        courseController.seedFromControls()
                    } ui {
                        fire(CourseUpdatedEvent)
                        fire(ZoomToFitCourseEvent)
                    }
                }
            }
            separator()
            item("Reverse Course Direction") {
                enableWhen(haveControls)
                action {
                    runAsync {
                        courseController.reverseCourse()
                    } ui {
                        fire(CourseUpdatedEvent)
                    }
                }
            }
            separator()
            item("Re Score Controls") {
                enableWhen(haveControls)
                action {
                    runAsync {
                        courseController.scoreControls()
                    }
                }
            }
        }
    }

}
