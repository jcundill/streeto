package org.streeto.ui

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.concurrent.Worker
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import tornadofx.*




class OpenLayersMapView : View("Map") {
    private val controller: CourseController by inject()
    private var showRouteChoice: Boolean = false
    val customFunction = JavaCallback(controller)

    class JavaCallback(private val controller: CourseController)  {
        fun controlMoved(num: String, lat: Double, lon: Double) {
            println("Control $num Moved to [$lat, $lon]")
            controller.moveControl(num, lat, lon)
        }
    }
    private fun WebView.initialiseEngine() {
        engine.loadWorker.stateProperty().addListener { ov: ObservableValue<*>?, oldState: Worker.State?, newState: Worker.State ->
            run {
                println( " called $oldState, $newState")
                if (newState === Worker.State.SUCCEEDED) {
                    val window: JSObject = engine.executeScript("window") as JSObject
                    window.setMember("theJavaFunction", customFunction)
                    println("theJavaFunction is set ")
                }
            }
        }
        engine.load(resources.url("/index.html").toExternalForm())
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
                onMouseClicked = EventHandler {
                    val ctrl = controller.getControlAt(mouseCoordinates, resolution)
                    if( ctrl != null) {
                        println("Selected Control: $ctrl" )
                    }
                }

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


