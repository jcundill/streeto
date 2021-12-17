package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Alert
import javafx.scene.control.MenuBar
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import org.streeto.ui.controls.ControlsView
import org.streeto.ui.coursedetails.CourseDetailsView
import org.streeto.ui.evolution.GenerationProgressView
import org.streeto.ui.legs.LegDetailsView
import org.streeto.ui.legs.LegsView
import org.streeto.ui.map.OpenLayersMapView
import org.streeto.ui.preferences.PreferencesView
import tornadofx.*

class StreetOWorkspace : Workspace("StreetO") {

    private val courseController: CourseController by inject()
    private val mapView: OpenLayersMapView by inject()

    private val haveControls = SimpleBooleanProperty(false)
    private val showRoute = SimpleBooleanProperty(false)
    private val showRouteChoice = SimpleBooleanProperty(false)

    init {
        header.items.clear()
        courseController.controlList.onChange { newValue ->
            haveControls.value = newValue.list.size > 0
        }
    }



    override fun onBeforeShow() {
        super.onBeforeShow()
        dock<OpenLayersMapView>()
        workspace.tabContainer.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        workspace.title = "StreetO"
    }

    override fun onDock() {
        super.onDock()
        leftDrawer.item(find(CourseDetailsView::class))
        leftDrawer.item(find(ControlsView::class))
        leftDrawer.item(find(LegDetailsView::class))
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
                workspace.openInternalWindow<AboutView>(closeButton = true)
            }
        }
    }

    private fun MenuBar.fileMenu() {
        menu("_File") {
            item("_New") {
                    enableWhen(courseController.isReady)
                    action {
                        find<NewCourseView>().openModal()
                        courseController.removeAllControls()
                        fire(CourseUpdatedEvent)
                    }
                }

            item("_Open") {
                enableWhen(courseController.isReady)
                action {
                    val all = FileChooser.ExtensionFilter("Any", "*.*")
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFile =
                        chooseFile("Open File", filters = arrayOf(all, kml, gpx), mode = FileChooserMode.Single)
                    courseFile.map {
                        val course = courseController.loadCourse(it)
                        val start = course.controls[0].location
                        val startPoint = Point(start.lat, start.lon)
                        if (courseController.loadMapDataAt(startPoint)) {
                            mapView.runAsyncWithOverlay {
                                courseController.initialiseCourse(course.controls)
                                courseController.analyseCourse()
                            } ui {
                                fire(CourseUpdatedEvent)
                                fire(ZoomToFitCourseEvent)
                            }
                        } else {
                            alert(Alert.AlertType.ERROR, "Error", "No Map Data for this position")
                        }
                    }
                }
            }

            separator()
            item("_Save") {

            }
            item("Save _As") {
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
            item("Create _MapRun Files") {
                action {
                    val directory = chooseDirectory(title = "KML+KMZ Save Location")
                    if (directory != null) {
                        mapView.runAsyncWithOverlay {
                            courseController.generateMapRunFiles(directory)
                        }
                    }
                }
            }
            item("Create Map _PDF") {
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
            item("E_xit", "Shortcut+Q") {
                action {
                    Platform.exit()
                }
            }
        }
    }

    private fun MenuBar.showMenu() {
        menu("_Show") {
            item("Toggle Show R_oute", "shortcut+O") {
                enableWhen(haveControls)
                action {
                    showRoute.value = !showRoute.value
                    fire(RouteVisibilityEvent(showRoute.value))
                }
            }
            item("Toggle Show Route _Choice", "shortcut+C") {
                enableWhen(haveControls)
                action {
                    showRouteChoice.value = !showRouteChoice.value
                    fire(RouteChoiceVisibilityEvent(showRouteChoice.value))
                 }
            }
            separator()
            item("_Preferences") {
                action {
                    workspace.openInternalWindow<PreferencesView>()
                }
            }
        }
    }

    private fun MenuBar.mapMenu() {
        menu("_Map") {
            item("_Reset Rotation").action {
                fire(ResetRotationEvent)
            }
            item("Zoom To Fit _Course") {
                enableWhen(haveControls)
                action {
                    fire(ZoomToFitCourseEvent)
                }
            }
            item("Zoom To Current _Leg") {
                enableWhen(haveControls)
                action {
                    fire(ZoomToFitLegEvent)
                }
            }
            item("Zoom to Ne_xt Leg", "shortcut+X") {
                enableWhen(haveControls)
                action {
                    fire(NextLegEvent)
                    fire(ZoomToFitLegEvent)
                }
            }
            item("Zoom to _Previous Leg", "shortcut+P") {
                enableWhen(haveControls)
                action {
                    fire(PreviousLegEvent)
                    fire(ZoomToFitLegEvent)
                }
            }
        }
    }


    private fun MenuBar.courseMenu() {
        menu("_Course") {
            item("Clear _Numbered Controls") {
                enableWhen(haveControls)
                action {
                    courseController.removeNumberedControls()
                    fire(CourseUpdatedEvent)
                }
            }
            item("Clear _All") {
                enableWhen(haveControls)
                action {
                    courseController.removeAllControls()
                    fire(CourseUpdatedEvent)
                }
            }
            separator()
            item("_Improve Existing Controls") {
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
            item("_Seed from Existing Controls") {
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
            item("_Reverse Course Direction") {
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
            item("Re S_core Controls") {
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
