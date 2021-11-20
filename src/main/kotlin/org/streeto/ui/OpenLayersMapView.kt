package org.streeto.ui

import javafx.collections.ListChangeListener
import javafx.concurrent.WorkerStateEvent
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import netscape.javascript.JSObject
import tornadofx.*


class OpenLayerMapController : Controller() {
    val name: String = "Brian"
}

class OpenLayersMapView : View("Map") {
    private val controller: CourseController by inject()
    private val legController: LegController by inject()
    private var showRouteChoice: Boolean = false

    fun WebView.initialiseEngine() {
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
            vgrow = Priority.ALWAYS
            isContextMenuEnabled = false
            initialiseEngine()
            val thunk = ThunkingLayer(engine)
            subscribe<ResetRotationEvent> {
                thunk.rotation = 0.0
            }
            subscribe<ZoomToFitCourseEvent> {
                thunk.zoomToBestFit()
            }
            subscribe<ZoomToFitLegEvent> {
                val leg = controller.selectedLeg.value
                if (leg != null) {
                    thunk.zoomToLeg(leg)
                }
            }
            subscribe<RouteVisibilityEvent> {
                val controls = controller.getControls()
                if (it.visible && controls.size > 2) {
                    thunk.drawRoute(controller.getRoute())
                } else {
                    thunk.clearRoute()
                }
            }

            subscribe<RouteChoiceVisibilityEvent> {
                showRouteChoice = it.visible
                val leg = controller.selectedLeg.value
                if( leg != null && showRouteChoice) {
                    thunk.drawRouteChoice(leg.routeChoice)
                } else {
                    thunk.clearRouteChoice()
                }
            }
            controller.getControls().addListener(ListChangeListener { _ ->
                thunk.clearCourse()
                thunk.drawCourse(controller.getControls())
                thunk.zoomToBestFit()
            })

            controller.selectedControl.addListener { _, _, newValue ->
                run {
                    println("changed to $newValue")
                    if (newValue != null) {
                        thunk.zoomToControl(newValue)
                    } else {
                        thunk.zoomToBestFit()
                    }
                }
            }

            controller.selectedLeg.addListener {_, _, newValue ->
                run {
                    thunk.clearRouteChoice()
                    if( newValue != null) {
                        thunk.zoomToLeg(newValue)
                        if( showRouteChoice ) {
                            thunk.drawRouteChoice(newValue.routeChoice)
                        }
                    }
                }
            }
        }
    }
}
