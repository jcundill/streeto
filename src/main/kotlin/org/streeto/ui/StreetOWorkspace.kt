package org.streeto.ui

import javafx.application.Platform
import javafx.stage.FileChooser
import tornadofx.*

class StreetOWorkspace() : Workspace("Editor", NavigationMode.Tabs) {

    private val courseController: CourseController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
        dock<OpenLayersMapView>()
    }
    override fun onDock() {
        super.onDock()
        leftDrawer.item( find(ControlsView::class) )
        rightDrawer.item(find(LegList::class))
    }

    init {
        menubar {
            menu("File") {
                item("New").action {
                    find<NewCourseView>().openModal()
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
                    val courseFile = chooseFile("Open File", filters = arrayOf(kml, gpx), mode = FileChooserMode.Save)
                    println(courseFile)
                }
                separator()
                item("Create MapRun Files")
                item("Create Map PDF")
                separator()
                item("Quit", "Shortcut+Q"){
                 }.action {
                    Platform.exit()
                }
            }
            menu("Course") {
                item("Create From Controls")
                item("Score Controls")
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
 //        children.add(DemoView(preferencesFx, this))
                }
            }
            menu("Help") {
                item("About").action {
                    openInternalWindow<AboutView>()
                }
            }
        }
    }
}
