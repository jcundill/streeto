package org.streeto.ui

import javafx.collections.ListChangeListener
import javafx.concurrent.WorkerStateEvent
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import tornadofx.*


class OpenLayersMapView : View("Map") {
    private val controller: CourseController by inject()
    private var showRouteChoice: Boolean = false

    private fun WebView.initialiseEngine() {
        engine.load(resources.url("/index.html").toExternalForm())
        engine.loadWorker.stateProperty().addListener { _, _, newValue ->
            run {
                if (newValue == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                    // now initialize the mapView
                    val window: JSObject = engine.executeScript("window") as JSObject
                    window.setMember("theJavaFunction", customFunction)

                }
            }
        }
    }


    val customFunction = object {
        fun function(data: Array<Any>): Any {
            val type = data[0] as String
            if (type == "CONTROL_MOVED") {
                println("Control Moved")
            }
            return true
        }
    }

    override val root = vbox {
        webview {
            contextmenu {
                item("sdfsf")
                item("sfsfdsdfsdfsdf")
                isAutoHide = true
            }
            vgrow = Priority.ALWAYS
            isContextMenuEnabled = false
            initialiseEngine()

            with(ThunkingLayer(engine)) {
                subscribe<ResetRotationEvent> {
                    rotation = 0.0
                }

                subscribe<ZoomToFitCourseEvent> {
                    zoomToBestFit()
                }

                subscribe<ZoomToFitLegEvent> {
                    val leg = controller.selectedLeg.value
                    if (leg != null) {
                        zoomToLeg(leg)
                    }
                }

                subscribe<RouteVisibilityEvent> {
                    val controls = controller.getControls()
                    if (it.visible && controls.size > 2) {
                        drawRoute(controller.getRoute())
                    } else {
                        clearRoute()
                    }
                }

                subscribe<RouteChoiceVisibilityEvent> {
                    showRouteChoice = it.visible
                    val leg = controller.selectedLeg.value
                    if (leg != null && showRouteChoice) {
                        drawRouteChoice(leg.routeChoice)
                    } else {
                        clearRouteChoice()
                    }
                }

                controller.getControls().addListener(ListChangeListener {
                    clearCourse()
                    if (controller.getControls().isNotEmpty()) {
                        drawCourse(controller.getControls())
                        zoomToBestFit()
                    }
                })

                controller.selectedControl.addListener { _, _, newValue ->
                    run {
                        println("changed to $newValue")
                        if (newValue != null) {
                            zoomToControl(newValue)
                        } else {
                            zoomToBestFit()
                        }
                    }
                }

                controller.selectedLeg.addListener { _, _, newValue ->
                    run {
                        clearRouteChoice()
                        if (newValue != null) {
                            zoomToLeg(newValue)
                            if (showRouteChoice) {
                                drawRouteChoice(newValue.routeChoice)
                            }
                        }
                    }
                }
            }
        }
    }
}


