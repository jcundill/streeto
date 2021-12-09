package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import tornadofx.*
import java.io.File
import java.util.*


class OpenLayersMapView : View("Map") {

    private val controller: CourseController by inject()
    private val controlModel: ControlViewModel by inject()
    private val legModel: ScoredLegModel by inject()

    val customFunction = JavaCallback(controller, this)
    private val isClickedOnControl = SimpleBooleanProperty(false)
    private val clickPosition = SimpleObjectProperty<Point>()
    private var showRouteOnMap: Boolean = false
    private var showRouteChoiceOnMap: Boolean = false

    override fun onBeforeShow() {
        this.deletableWhen { never }
    }

    class JavaCallback(private val controller: CourseController, private val view: View) {
        fun controlMoved(num: String, lat: Double, lon: Double) {
            controller.moveControl(num, lat, lon)
            view.fire(CourseUpdatedEvent)
        }
    }

    private fun WebView.initialiseEngine() {
        engine.loadWorker.stateProperty().addListener { _, _, newState: Worker.State ->
            run {
                if (newState === Worker.State.SUCCEEDED) {
                    val window: JSObject = engine.executeScript("window") as JSObject
                    window.setMember("theJavaFunction", customFunction)

                    val args = app.parameters.named
                    if (!args["props"].isNullOrEmpty()) {
                        val props = Properties()
                        props.load(File(args["props"]!!).inputStream())
                        controller.initializeGH(props)
                    }
                    if (!args["course"].isNullOrEmpty()) {
                        controller.loadCourse(File(args["course"]!!))
                    }
                }
            }
        }
        engine.load(resources.url("/index.html").toExternalForm())
    }


    override val root = vbox {
        webview {
            vgrow = Priority.ALWAYS
            isContextMenuEnabled = false
            initialiseEngine()

            contextmenu {
                item("Details") {
                    visibleWhen(isClickedOnControl)
                    action {
                        find<ControlDetailView>().openModal()
                    }
                }
                item("Place Start Here") {
                    action {
                        controller.setStartAt(clickPosition.value)
                        fire(CourseUpdatedEvent)
                    }
                }
                item("Place Finish Here") {
                    action {
                        controller.setFinishAt(clickPosition.value)
                        fire(CourseUpdatedEvent)
                    }
                }
                item("Split Leg After") {
                    action {
                        controller.splitLegAfterSelected()
                        fire(CourseUpdatedEvent)
                    }
                }
                item("Split Leg Before") {
                    action {
                        controller.splitLegBeforeSelected()
                        fire(CourseUpdatedEvent)
                    }
                }
                item("Remove Control") {
                    action {
                        controller.removeSelectedControl()
                        fire(CourseUpdatedEvent)
                    }
                }
                isAutoHide = true
            }

            with(ThunkingLayer(engine)) {

                fun drawOverlays() {
                    clearRoute()
                    if (showRouteOnMap) {
                        val controls = controller.controlList
                        if (controls.size > 2) {
                            drawRoute(controller.getRoute())
                        }
                    }
                    clearRouteChoice()
                    if (showRouteChoiceOnMap) {
                        val leg = legModel.item
                        if (leg != null) {
                            drawRouteChoice(leg.routeChoice)
                        }
                    }
                }

                controller.isReady.onChange { ready ->
                    if (ready) {
                        zoomToDataBounds(controller.dataBounds)
                    }
                }
                onMouseClicked = EventHandler {
                    val coords = mouseCoordinates
                    val ctrl = controller.getControlAt(coords, resolution)
                    controlModel.item = ctrl
                    isClickedOnControl.value = ctrl != null
                    clickPosition.value = coords
                }

                subscribe<ResetRotationEvent> {
                    rotation = 0.0
                }

                subscribe<ZoomToFitCourseEvent> {
                    zoomToBestFit()
                }

                subscribe<ZoomToFitLegEvent> {
                    val leg = legModel.item
                    if (leg != null) {
                        zoomToLeg(leg)
                    } else {
                        println("leg model is null")
                    }
                }

                subscribe<RouteVisibilityEvent> {
                    showRouteOnMap = it.visible
                    drawOverlays()
                }

                subscribe<RouteChoiceVisibilityEvent> {
                    showRouteChoiceOnMap = it.visible
                    drawOverlays()
                }

                subscribe<CourseUpdatedEvent> {
                    if (controller.controlList.isNotEmpty()) {
                        drawCourse(controller.controlList)
                        drawOverlays()
                    }
                }

                subscribe<ZoomToControlEvent> {
                    println("zoom to ${it.control}")
                    if (it.control != null) {
                        zoomToControl(it.control)
                    }
                }

                legModel.start.addListener { _, _, newValue ->
                    run {
                        clearRouteChoice()
                        if (newValue != null) {
                            if (legModel.item.start.number != newValue.number) {
                                zoomToLeg(legModel.item)
                            }
                            if (showRouteChoiceOnMap) {
                                drawRouteChoice(legModel.item.routeChoice)
                            }
                        }
                    }
                }
            }
        }
    }
}


