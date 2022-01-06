package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.MenuBar
import javafx.scene.control.TabPane
import javafx.stage.FileChooser
import org.streeto.ControlSite
import org.streeto.ui.about.AboutView
import org.streeto.ui.controls.ControlsView
import org.streeto.ui.coursedetails.CourseDetailsView
import org.streeto.ui.evolution.GenerationProgressView
import org.streeto.ui.geocode.NominatimChecker
import org.streeto.ui.geocode.NominatimView
import org.streeto.ui.legs.LegDetailsView
import org.streeto.ui.legs.LegsView
import org.streeto.ui.map.OpenLayersMapView
import org.streeto.ui.map.StreetOActions
import org.streeto.ui.osmdata.OsmDataView
import org.streeto.ui.preferences.PreferencesView
import tornadofx.*
import java.io.File
import java.util.*

class StreetOWorkspace : Workspace("StreetO") {

    private val courseController: CourseController by inject()
    private val mapView: OpenLayersMapView by inject()

    private val haveControls = SimpleBooleanProperty(false)
    private val haveNumberedControls = SimpleBooleanProperty(false)
    private val showRoute = SimpleBooleanProperty(false)
    private val showRouteChoice = SimpleBooleanProperty(false)
    private val isNominatimOk = SimpleBooleanProperty(false)
    private val nominatimChecker = NominatimChecker()

    init {
        isNominatimOk.value = nominatimChecker.isOkToUseNominatim()
        header.items.clear()
        courseController.controlList.onChange { newValue ->
            haveControls.value = newValue.list.size > 1
            haveNumberedControls.value = newValue.list.size > 2
        }
        courseController.courseFile.onChange { newValue ->
            workspace.title = "StreetO - " + (newValue?.path ?: "Untitled")
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
        menu("_Help") {
            item("User Guide") {
                action {
                    hostServices.showDocument("https://jcundill.github.io/streeto/index.html")
                }
            }
            item("About").action {
                find<AboutView>().openModal()
            }
        }
    }

    private fun MenuBar.fileMenu() {
        menu("_File") {
            menu("_New") {
                item("Course Here") {
                    enableWhen(courseController.isReady)
                    action {
                        courseController.removeAllControls()
                        courseController.courseFile.value = null
                        fire(CourseUpdatedEvent)
                    }
                }
                item("Course Location") {
                    enableWhen(isNominatimOk.and(courseController.isReady))
                    action {
                        courseController.removeAllControls()
                        courseController.courseFile.value = null
                        fire(CourseUpdatedEvent)
                        find<NominatimView>().openModal()
                    }
                }
            }
            item("_Open") {
                enableWhen(courseController.isReady)
                action {
                    val all = FileChooser.ExtensionFilter("Course Files", "*.kml", "*.gpx")
                    val kml = FileChooser.ExtensionFilter("KML", "*.kml")
                    val gpx = FileChooser.ExtensionFilter("GPX", "*.gpx")
                    val courseFiles =
                        chooseFile("Open File", filters = arrayOf(all, kml, gpx), mode = FileChooserMode.Single)
                    if (courseFiles.isNotEmpty()) {
                        val file = courseFiles[0]
                        val course = courseController.loadCourse(file)
                        if (course != null) {
                            val start = course.controls[0].location
                            val startPoint = Point(start.lat, start.lon)
                            val onLoaded = {
                                courseController.initialiseCourse(course.controls)
                                courseController.analyseCourse()
                                courseController.setDetailsFrom(file)
                                fire(CourseUpdatedEvent)
                                fire(ZoomToFitCourseEvent)
                                fire(ControlSelectedEvent(courseController.controlList[0]))
                            }
                            StreetOActions.loadMapDataAction(courseController, mapView, startPoint, onLoaded)
                        } else {
                            alert(
                                type = Alert.AlertType.ERROR,
                                header = "Error",
                                content = "Could not load a course from file ${file.path}"
                            )
                        }
                    }
                }
            }
            separator()
            item("_Save") {
                enableWhen(courseController.courseFile.isNotNull.and(haveControls))
                action {
                    courseController.saveAs(courseController.courseFile.value)
                }
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
                enableWhen(haveNumberedControls)
                action {
                    val path = courseController.courseFile.value?.parentFile?.absolutePath ?: ""
                    val name = courseController.courseName.value ?: ""
                    val mapRunFiles = FileChooser.ExtensionFilter("MapRun Files", "*.kml", "*.kmz")
                    val fileSelection = chooseFile(
                        title = "Save MapRun Files As", arrayOf(mapRunFiles),
                        mode = FileChooserMode.Save,
                        initialFileName = name,
                        initialDirectory = File(path)
                    )
                    if (fileSelection.isNotEmpty()) {
                        mapView.runAsyncWithOverlay {
                            courseController.generateMapRunFiles(
                                fileSelection.first().parentFile,
                                fileSelection.first().nameWithoutExtension
                            )
                        } ui { written ->
                            if (written) {
                                val filename = fileSelection.first().nameWithoutExtension
                                alert(
                                    Alert.AlertType.INFORMATION,
                                    "MapRun Files Created",
                                    "$filename.kmz and $filename.kml written to ${fileSelection.first().parentFile.absolutePath}"
                                )
                            } else {
                                alert(Alert.AlertType.ERROR, "Error", "Error creating MapRun Files")
                            }
                        }
                    }
                }
            }
            item("Create Map _PDF") {
                enableWhen(haveNumberedControls)
                action {
                    val path = courseController.courseFile.value?.parentFile?.absolutePath ?: ""
                    val name = courseController.courseName.value ?: ""
                    val pdf = FileChooser.ExtensionFilter("PDF", "*.pdf")
                    val fileSelection = chooseFile(
                        title = "Save PDF Map As", arrayOf(pdf),
                        mode = FileChooserMode.Save,
                        initialFileName = if (name.isNotBlank()) "$name.pdf" else "",
                        initialDirectory = File(path)
                    )
                    if (fileSelection.isNotEmpty()) {
                        mapView.runAsyncWithOverlay {
                            courseController.generatePDF(fileSelection[0])
                        } ui { written ->
                            if (written) {
                                hostServices.showDocument(fileSelection[0].toURI().toString())
                            } else {
                                alert(Alert.AlertType.ERROR, "Error", "Error creating PDF Map")
                            }
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
            item("Toggle Show Route _Choice", "shift+shortcut+O") {
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
            item("Select _Next Leg", "DOWN") {
                enableWhen(haveControls)
                action {
                    fire(NextLegEvent)
                }
            }
            item("Select _Previous Leg", "UP") {
                enableWhen(haveControls)
                action {
                    fire(PreviousLegEvent)
                }
            }
            item("Zoom to Ne_xt Leg", "shortcut+DOWN") {
                enableWhen(haveNumberedControls)
                action {
                    fire(NextLegEvent)
                    fire(ZoomToFitLegEvent)
                }
            }
            item("Zoom to Pre_vious Leg", "shortcut+UP") {
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
