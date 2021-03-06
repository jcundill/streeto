package org.streeto.ui.map

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.concurrent.Worker
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import net.harawata.appdirs.AppDirsFactory
import netscape.javascript.JSObject
import org.streeto.ui.*
import org.streeto.ui.controls.ControlDetailView
import org.streeto.ui.map.StreetOActions.loadMapDataAction
import tornadofx.*
import java.io.File
import java.util.*


class OpenLayersMapView : StreetOView("Map") {

    private val haveControls = SimpleBooleanProperty(false)
    private val controller: CourseController by inject()
    private val controlModel: ControlViewModel by inject()
    private val legModel: ScoredLegModel by inject()

    private val customFunction = JavaCallback(controller, this)
    private val isClickedOnControl = SimpleBooleanProperty(false)
    private val clickPosition = SimpleObjectProperty<Point>()
    private var showRouteOnMap: Boolean = false
    private var showRouteChoiceOnMap: Boolean = false

    init {
        controller.controlList.onChange { newValue ->
            haveControls.value = newValue.list.size > 1
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
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
                        controller.initializeGH(props.getProperty("osmDir"))
                    } else {
                        val props = Properties()
                        val dataDir = AppDirsFactory.getInstance().getUserDataDir("StreetO", null, "org.streeto")
                        props.setProperty("osmDir", dataDir)
                        controller.initializeGH(dataDir)
                    }
                }
            }
        }
        val location = resources.url("/index.html").toExternalForm()
        engine.load(location)
    }

    override val root = vbox {
        webview {
            vgrow = Priority.ALWAYS
            isContextMenuEnabled = false
            initialiseEngine()

            contextmenu(contextMenu())

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
                        val lastCenter = controller.getLastLocation()
                        if (lastCenter != Point(0.0, 0.0)) {
                            zoomToPoint(lastCenter, 15)
                            resolution = controller.getLastResolution()
                        } else {
                            // show the whole world
                            zoomToPoint(lastCenter, 1)
                        }
                    }
                }

                onMouseClicked = EventHandler {
                    val coords = mouseCoordinates
                    val ctrl = controller.getControlAt(coords, resolution)
                    controlModel.item = ctrl
                    isClickedOnControl.value = ctrl != null
                    clickPosition.value = coords
                    if (ctrl != null) {
                        fire(ControlSelectedEvent(ctrl))
                    }
                }

                if (!Styles.isMac()) {
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code == KeyCode.DOWN || it.code == KeyCode.UP) {
                            if (it.code == KeyCode.DOWN) {
                                fire(NextLegEvent)
                            } else {
                                fire(PreviousLegEvent)
                            }
                            it.consume()
                            if (it.isShortcutDown) {
                                fire(ZoomToFitLegEvent)
                            }
                        }
                    }
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

                subscribe<RouteChoiceSelectedEvent> {
                    drawOverlays()
                    drawSelectedRouteChoice(it.choice)
                }

                subscribe<CourseUpdatedEvent> {
                    drawCourse(controller.controlList)
                    if (controller.controlList.isNotEmpty()) {
                        drawOverlays()
                        // have we moved the start somewhere else
                        // if so, we need to update the stored last location used
                        if (controller.getLastLocation() != controller.controlList.first()) {
                            controller.saveLastLocation(controller.controlList[0], resolution)
                        }
                    }
                }

                subscribe<ZoomToControlEvent> {
                    if (it.control != null) {
                        zoomToControl(it.control)
                    }
                }

                subscribe<NewMapLocationEvent> {
                    val point = Point(it.lat, it.lon)
                    zoomToPoint(point, 16)
                    controller.saveLastLocation(point, resolution)
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

    private fun contextMenu(): ContextMenu.() -> Unit = {
        item("Details") {
            visibleWhen(isClickedOnControl)
            action {
                find<ControlDetailView>().openModal()
            }
        }
        item("Place Start Here") {
            visibleWhen(isClickedOnControl.not().or(haveControls.and(controlModel.number.isEqualTo("F1"))))
            action {
                val onLoaded = {
                    if (controller.setStartAt(clickPosition.value)) {
                        fire(CourseUpdatedEvent)
                    }
                }
                loadMapDataAction(controller, this@OpenLayersMapView, clickPosition.value, onLoaded)
            }
        }
        item("Place Finish Here") {
            visibleWhen(haveControls.and((controlModel.number.isEqualTo("S1").or(isClickedOnControl.not()))))
            action {
                controller.setFinishAt(clickPosition.value)
                fire(CourseUpdatedEvent)
            }
        }
        item("Split Leg After") {
            visibleWhen(isClickedOnControl)
            enableWhen(controlModel.number.isNotEqualTo("F1"))
            action {
                controller.splitLegAfterSelected()
                fire(CourseUpdatedEvent)
            }
        }
        item("Split Leg Before") {
            visibleWhen(isClickedOnControl)
            enableWhen(controlModel.number.isNotEqualTo("S1"))
            action {
                controller.splitLegBeforeSelected()
                fire(CourseUpdatedEvent)
            }
        }
        item("Remove Control") {
            visibleWhen(isClickedOnControl)
            enableWhen(controlModel.number.isNotEqualTo("S1").and(controlModel.number.isNotEqualTo("F1")))
            action {
                controller.removeSelectedControl()
                fire(CourseUpdatedEvent)
            }
        }
        isAutoHide = true
    }
}


