package org.streeto.ui

import com.graphhopper.util.shapes.BBox
import javafx.scene.web.WebEngine
import java.util.stream.Collectors


class ThunkingLayer(val browser: WebEngine) {

    var resolution: Double
        get() = callOLFunction("getResolution();") as Double
        set(resolution) {
            callOLFunction("setResolution($resolution)")
        }
    var rotation: Double
        get() = callOLFunction("getRotation();") as Double
        set(value) {
            callOLFunction("setRotation($value)")
        }
    var mapCenter: Point?
        get() = asPoint(callOLFunction("getCenter();"))
        set(center) {
            callOLFunction("setCenter(${center.toString()})")
        }

    val mouseCoordinates: Point
        get() = asPoint(callMapFunction("getMouseCoords();"))

    fun zoomToDataBounds(bounds: BBox) {
        println(bounds)
        val a = Point(bounds.minLat, bounds.minLon)
        val b = Point(bounds.maxLat, bounds.maxLon)
        val ans = callMapFunction("zoomToFitBounds(${asLatLonObj(a)}, ${asLatLonObj(b)})")
        println(ans)
    }

    fun zoomToBestFit() = callMapFunction("zoomToFitCourse()")

    fun clearCourse() = callMapFunction("clearCourse()")

    fun addControl(location: Control) = callMapFunction("addControls(${asLatLon(location)})")

    fun addLine(start: Control, end: Control) = callMapFunction("addLine(${asLatLon(start)}, ${asLatLon(end)})")

    fun drawCourse(controls: List<Control>) {
        val str = controls.stream()
            .map(this::asNumberedControl)
            .collect(Collectors.joining(",", "[", "]"))

        callMapFunction("drawCourse($str, document.courseSource);")
    }

    fun drawRoute(route: List<Point>) {
        clearRoute()
        callMapFunction("drawRoute(${asPointList(route)}, document.routeSource);")
    }

    fun drawRouteChoice(routes: List<PointList>) {
        clearRouteChoice()
        routes.forEach { route: PointList ->
            callMapFunction("drawRoute(${asPointList(route.points)}, document.routeChoiceSource);")
        }
    }

    fun zoomToLeg(leg: CourseLeg) {
        val str = listOf(leg.start, leg.end).stream()
            .map(this::asNumberedControl)
            .collect(Collectors.joining(",", "[", "]"))

        callMapFunction("zoomToFitLeg($str);")
    }

    fun clearRoute() = callJavascript("document.routeSource.clear();")

    fun clearRouteChoice() = callJavascript("document.routeChoiceSource.clear();")

    fun zoomToControl(control: Control, level: Int = 19) =
        callMapFunction("zoomToLatLon(${asLatLonObj(control)}, $level);")

    private fun callJavascript(str: String): Any = browser.executeScript(str)

    private fun callMapFunction(str: String): Any = callJavascript("document.streetoMap.$str")

    private fun callOLFunction(str: String): Any = callJavascript("document.streetoMap.getView().$str")

    private fun asLatLon(location: Point) = "${location.lat},${location.lon}"

    private fun asPointList(route: List<Point>) = route.stream()
        .map(this::asLatLonObj)
        .collect(Collectors.joining(",", "[", "]"))

    private fun asLatLonObj(ctrl: Point) = "{lat:${ctrl.lat}, lon:${ctrl.lon}}"

    private fun asNumberedControl(ctrl: Control) =
        "{lat:${ctrl.lat}, lon:${ctrl.lon}, number:'${ctrl.number}'}"

    private fun asPoint(ans: Any): Point {
        val ret = ans.toString().split(",") // lon, lat
        val lat = ret[1].toDouble()
        val lon = ret[0].toDouble()
        return Point(lat, lon)
    }
}