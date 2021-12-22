package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.MenuBar
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import org.streeto.ControlSite
import org.streeto.ui.controls.ControlsView
import org.streeto.ui.coursedetails.CourseDetailsView
import org.streeto.ui.evolution.GenerationProgressView
import org.streeto.ui.legs.LegDetailsView
import org.streeto.ui.legs.LegsView
import org.streeto.ui.map.OpenLayersMapView
import org.streeto.ui.osmdata.OsmDataView
import org.streeto.ui.preferences.PreferencesView
import tornadofx.*
import java.util.*

class StreetOWorkspace : Workspace("StreetO") {

    private val courseController: CourseController by inject()
    private val mapView: OpenLayersMapView by inject()

    private val haveControls = SimpleBooleanProperty(false)
    private val haveNumberedControls = SimpleBooleanProperty(false)
    private val showRoute = SimpleBooleanProperty(false)
    private val showRouteChoice = SimpleBooleanProperty(false)

    init {
        header.items.clear()
        courseController.controlList.onChange { newValue ->
            haveControls.value = newValue.list.size > 1
            haveNumberedControls.value = newValue.list.size > 2
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
                        val haveData = courseController.hasMapDataFor(startPoint)
                        var doLoad = false
                        if (!haveData) {
                            confirm(
                                "No map data found for this position",
                                "Load it now? This will take a few minutes"
                            ) {
                                doLoad = true
                            }
                        }
                        if (haveData || doLoad) {
                            mapView.runAsyncWithOverlay {
                                courseController.loadMapDataAt(startPoint, doLoad)
                            } ui { loaded ->
                                if (loaded) {
                                    courseController.initialiseCourse(course.controls)
                                    courseController.analyseCourse()
                                    fire(CourseUpdatedEvent)
                                    fire(ZoomToFitCourseEvent)
                                }
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
                enableWhen(haveControls)
                action {
                    val all = FileChooser.ExtensionFilter("Any", "*.*")
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFiles =
                        chooseFile("Save Course As", filters = arrayOf(all, kml, gpx), mode = FileChooserMode.Save)
                    if (courseFiles.isNotEmpty()) {
                        courseController.saveAs(courseFiles[0])
                    }
                }
            }
            separator()
            item("Create _MapRun Files") {
                enableWhen(haveControls)
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
                enableWhen(haveControls)
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
                    find<PreferencesView>().openModal()
                }
            }
            item("Imported _Data") {
                action {
                    val osmView = find<OsmDataView>()
                    osmView.onBeforeShow()
                    osmView.openModal()
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
                enableWhen(haveNumberedControls)
                action {
                    fire(NextLegEvent)
                    fire(ZoomToFitLegEvent)
                }
            }
            item("Zoom to _Previous Leg", "shortcut+P") {
                enableWhen(haveNumberedControls)
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
                enableWhen(haveNumberedControls)
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
                enableWhen(haveNumberedControls)
                action {
                    generateFromControls(CourseController::generateFromControls)
                }
            }
            item("_Seed from Existing Controls") {
                enableWhen(haveControls)
                action {
                    showSeedDialog()
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

    private fun showSeedDialog() {
        dialog("Seed from Existing Controls") {
            field("Preferred Distance") {
                textfield(courseController.requestedDistance)
            }
            field("Course Title") {
                textfield(courseController.courseName)
            }
            hbox {
                alignment = Pos.CENTER_RIGHT
                button("OK") {
                    isDefaultButton = true
                    action {
                        Platform.runLater {
                            generateFromControls(CourseController::seedFromControls)
                        }
                        this@dialog.close()
                    }
                }
                button("Cancel") {
                    isCancelButton = true
                    action {
                        this@dialog.close()
                    }
                }
            }
        }
    }

    private fun generateFromControls(function: (CourseController) -> Optional<List<ControlSite>>) {
        if (!courseController.isCurrentCourseLengthValid()) {
            val maxAllowed = courseController.getMaxAllowedCourseLength()
            alert(
                Alert.AlertType.WARNING, "Course Too Long",
                "The existing course is longer than that allowed by the current preferences.\n" +
                        "Existing course must be less than $maxAllowed metres in length.\n" +
                        "\n" +
                        "Please make it shorter before continuing."
            )
            return
        }
        if (courseController.controlList.size > 1) {//need at least start and finish
            courseController.sniffer.reset()
            find<GenerationProgressView> {
                model.finished.value = false
                closeableWhen { courseController.sniffer.completedProperty }
                openModal()
            }
            runAsync {
                function.invoke(courseController)
            } ui { maybeSites ->
                if (maybeSites.isPresent) {
                    val sites = maybeSites.get()
                    courseController.initialiseCourse(sites)
                    courseController.analyseCourse()

                    fire(CourseUpdatedEvent)
                    fire(ZoomToFitCourseEvent)
                }
            }
        } else {
            alert(
                Alert.AlertType.WARNING, "No Controls",
                "There are no controls in the course.\n" +
                        "Please add at least a start and finish control before continuing."
            )
        }
    }

}
