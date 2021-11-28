package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ListChangeListener
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
    private var showRouteChoice: Boolean = false
    val controlModel: ControlViewModel by inject()
    val legModel: ScoredLegModel by inject()

    val customFunction = JavaCallback(controller)
    val clickedOnControl = SimpleBooleanProperty(false)

    class JavaCallback(private val controller: CourseController) {
        fun controlMoved(num: String, lat: Double, lon: Double) {
            println("Control $num Moved to [$lat, $lon]")
            controller.moveControl(num, lat, lon)
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
            contextmenu {
                item("Details") {
                    visibleWhen(clickedOnControl)
                    action {
                        workspace.openInternalWindow<ControlDetailView>(modal = false)
                    }
                }
                item("sfsfdsdfsdfsdf")
                isAutoHide = true
            }
            vgrow = Priority.ALWAYS
            isContextMenuEnabled = false
            initialiseEngine()

            with(ThunkingLayer(engine)) {

                controller.isReady.onChange { ready ->
                    if (ready) {
                        zoomToDataBounds(controller.dataBounds)
                    }
                }
                onMouseClicked = EventHandler {
                    val ctrl = controller.getControlAt(mouseCoordinates, resolution)
                    controlModel.item = ctrl
                    clickedOnControl.value = ctrl != null
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
                    val controls = controller.controlList
                    if (it.visible && controls.size > 2) {
                        drawRoute(controller.getRoute())
                    } else {
                        clearRoute()
                    }
                }

                subscribe<RouteChoiceVisibilityEvent> {
                    showRouteChoice = it.visible
                    val leg = legModel.item
                    if (leg != null && showRouteChoice) {
                        drawRouteChoice(leg.routeChoice)
                    } else {
                        clearRouteChoice()
                    }
                }

                controller.controlList.addListener(ListChangeListener {
                    clearCourse()
                    if (controller.controlList.isNotEmpty()) {
                        drawCourse(controller.controlList)
                        zoomToBestFit()
                    }
                })

                subscribe<ZoomToControlEvent> {
                    println("zoom to ${it.control}")
                    if (it.control != null) {
                        zoomToControl(it.control)
                    }
                }

                legModel.start.addListener() { _, _, newValue ->
                    run {
                        clearRouteChoice()
                        if (newValue != null) {
                            zoomToLeg(legModel.item)
                            if (showRouteChoice) {
                                drawRouteChoice(legModel.item.routeChoice)
                            }
                        }
                    }
                }
            }
        }
    }
}


