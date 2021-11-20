package org.streeto.ui

import javafx.scene.web.WebEngine
import java.util.function.Consumer
import java.util.stream.Collectors


class ThunkingLayer(val browser: WebEngine) {
    private fun evaluateJavascript(str: String): Any {
        return browser.executeScript(str)
    }

    private fun callJavascript(str: String): Any {
        return browser.executeScript(str)
    }

    var resolution: String?
        get() = evaluateJavascript("return document.streetoMap.getView().getResolution();").toString()
        set(resolution) {
            val cmd = String.format("document.streetoMap.getView().setResolution(%s)", resolution)
            callJavascript(cmd)
        }
    var rotation: Double
        get() = evaluateJavascript("return document.streetoMap.getView().getRotation();") as Double
        set(value) {
            val cmd = String.format("document.streetoMap.getView().setRotation(%f)", value)
            callJavascript(cmd)
        }
    var mapCenter: String?
        get() {
            val arr = evaluateJavascript("return document.streetoMap.getView().getCenter();") as Array<*>
            return String.format("[%s, %s]", arr[0].toString(), arr[1].toString())
        }
        set(center) {
            val cmd = String.format("document.streetoMap.getView().setCenter(%s)", center)
            callJavascript(cmd)
        }

    fun zoomToBestFit() {
        val cmd = "document.streetoMap.zoomToFitCourse()"
        callJavascript(cmd)
    }

    fun clearCourse() {
        val cmd = "document.streetoMap.clearCourse()"
        callJavascript(cmd)
    }

    fun addControl(location: Control) {
        val cmd = String.format("document.streetoMap.addControls(%f,%f)", location.lat, location.lon)
        callJavascript(cmd)
    }

    fun addLine(start: Control, end: Control) {
        val cmd = String.format(
            "document.streetoMap.addLine(%f,%f, %f, %f)", start.lat, start.lon, end.lat,
            end.lon
        )
        callJavascript(cmd)
    }

    fun drawCourse(controls: List<Control>) {
        val str = controls.stream().map { ctrl: Control ->
            String.format(
                "{lat:%f, lon:%f, number:'%s'}",
                ctrl.lat, ctrl.lon, ctrl.number
            )
        }
            .collect(Collectors.joining(",", "[", "]"))
        val cmd = String.format("document.streetoMap.drawCourse(%s, document.courseSource);", str)
        callJavascript(cmd)
    }

    fun drawRoute(route: List<Point>) {
        clearRoute()
        val str = route.stream().map { ctrl ->
            String.format(
                "{lat:%f, lon:%f}",
                ctrl.lat,
                ctrl.lon
            )
        }.collect(Collectors.joining(",", "[", "]"))
        val cmd = String.format("document.streetoMap.drawRoute(%s, document.routeSource);", str)
        callJavascript(cmd)
    }

    fun drawRouteChoice(routes: List<PointList>) {
        clearRouteChoice()
        routes.forEach(Consumer { route: PointList ->
            val str = route.points.stream()
                .map { ctrl: Point ->
                    String.format(
                        "{lat:%f, lon:%f}",
                        ctrl.lat,
                        ctrl.lon
                    )
                }
                .collect(Collectors.joining(",", "[", "]"))
            val cmd =
                String.format("document.streetoMap.drawRoute(%s, document.routeChoiceSource);", str)
            callJavascript(cmd)
        })
    }

    fun zoomToLeg(leg: CourseLeg) {
        val str = listOf(leg.start, leg.end).stream().map { ctrl: Control ->
            String.format(
                "{lat:%f, lon:%f, number:'%s'}",
                ctrl.lat, ctrl.lon, ctrl.number
            )
        }.collect(Collectors.joining(",", "[", "]"))
        val cmd = String.format("document.streetoMap.zoomToFitLeg(%s);", str)
        callJavascript(cmd)
    }

    // lon, lat
    val mouseCoordinates: Control
        get() {
            val cmd = "return document.streetoMap.getMouseCoords();"
            val ans = evaluateJavascript(cmd)
            val ret = ans as Array<*> // lon, lat
            val lat = ret[1] as Double
            val lon = ret[0] as Double
            return Control("", "", lat, lon)
        }

    fun clearRoute() {
        val cmd = "document.routeSource.clear();"
        callJavascript(cmd)
    }

    fun clearRouteChoice() {
        val cmd = "document.routeChoiceSource.clear();"
        callJavascript(cmd)
    }

    val controlNumberUnderMouse: String
        get() {
            val cmd = "return document.streetoMap.getControlUnderMouse();"
            return evaluateJavascript(cmd) as String
        }

    fun zoomToControl(control: Control) {
        val str = String.format("{lat:%f, lon:%f}", control.lat, control.lon)
        val cmd = String.format("document.streetoMap.zoomToLatLon(%s);", str)
        callJavascript(cmd)
    }


}