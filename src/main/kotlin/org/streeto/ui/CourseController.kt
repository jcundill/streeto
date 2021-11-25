package org.streeto.ui

import com.graphhopper.ResponsePath
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.streeto.ControlSite
import org.streeto.StreetO
import org.streeto.utils.CollectionHelpers
import org.streeto.utils.CollectionHelpers.first
import org.streeto.utils.CollectionHelpers.last
import tornadofx.*
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.streams.toList

class CourseController : Controller() {
    private var selected = SimpleObjectProperty<Control?>()
    var selectedLeg = SimpleObjectProperty<RoutedLeg?>()
    private var controlList = SortedFilteredList<Control>()
    private var legList = SortedFilteredList<RoutedLeg>()
    private var controlSiteList = SortedFilteredList<ControlSite>()
    private lateinit var streetO : StreetO
    var isReady = SimpleBooleanProperty(false)

    fun initializeGH(properties: Properties) {
        streetO = StreetO( properties.getProperty("pbfFile"), properties.getProperty("graphDir"))
        isReady.value = true
    }

    fun loadCourse(file: File) {
        val course = if (file.extension == "gpx") {
            streetO.importer.buildFromGPX(file.path)
        } else {
            streetO.importer.buildFromKml(file.inputStream())
        }
        initialiseCourse(course.controls)
    }

    private fun initialiseCourse(controls: MutableList<ControlSite>) {
        controlSiteList.clear()
        controlSiteList.addAll(controls)
        val legs = CollectionHelpers.windowed(controls, 2).map {
            val start = first(it)
            val end = last(it)
            val routeChoices = streetO.getLegRoutes(start, end)
            val pointLists = routeChoices.map { path ->
                val points = path.points.map { p -> Point(p.lat, p.lon) }
                PointList(points)
            }
            val startControl = toControl(start)
            val endControl = toControl(end)
            RoutedLeg(startControl, endControl, pointLists)
        }.toList()
        legList.clear()
        legList.addAll(legs)
        //return@task course
        //} ui {
        getControls().clear()

        controls
            .map { c -> toControl(c) }
            .forEach {
                getControls().add(it)
            }
        // }
    }

    private fun toControl(start: ControlSite) =
        Control(start.number, start.description, start.location.lat, start.location.lon)

    fun getControls(): SortedFilteredList<Control> {
        return controlList
    }


    fun getRoute(): List<Point> {
        val path: ResponsePath = streetO.routeControls(controlSiteList)
        return CollectionHelpers.iterableAsStream(path.points)
            .map { point -> Point(point.lat, point.lon) }
            .collect(Collectors.toList())
    }

    fun selectControl(ctrl: Control?) {
        selectedControl.value = ctrl
    }

    fun selectLegTo(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            val leg = CourseLeg(controlList[idx - 1], control)
            selectedLeg.value = legList.first { it.start == leg.start && it.end == leg.end }
        }
    }

    fun selectLegFrom(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            val leg = CourseLeg(control, controlList[idx + 1])
            selectedLeg.value = legList.first { it.start == leg.start && it.end == leg.end }
        }
    }

    fun generateFromControls() {
        val initial = mutableListOf(first(controlSiteList), last(controlSiteList))
        runAsync {
            streetO.generateCourse(6000.0, 10, initial)
        } ui { maybeCourse ->
            if (maybeCourse.isPresent) {
                initialiseCourse(maybeCourse.get())

            }
        }
    }

    fun getControlAt(point: Point, res: Double): Control? {
        return controlList.find { c -> c.lat.nearly(point.lat, res) && c.lon.nearly(point.lon, res) }
    }

    fun Double.nearly(other: Double, res: Double) = abs(this - other) < 0.00020 * res
    fun moveControl(num: String, lat: Double, lon: Double) {
        with( controlSiteList.find { it.number == num } ) {
            if( this != null) {
                this.location.lat = lat
                this.location.lon = lon
                initialiseCourse(controlSiteList.toMutableList())
            }
        }
    }


    val selectedControl: SimpleObjectProperty<Control?>
        get() {
            return selected
        }

}