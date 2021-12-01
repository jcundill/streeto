package org.streeto.ui

import com.graphhopper.ResponsePath
import com.graphhopper.util.shapes.BBox
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
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
    var controlList = SortedFilteredList<Control>()
    var legList = SortedFilteredList<ScoredLeg>()

    val preferencesController: PreferencesHandler by inject()
    private val preferencesViewModel: PreferencesViewModel by inject()

    private var controlSiteList = SortedFilteredList<ControlSite>()
    private var streetO: StreetO by singleAssign()
    val preferences = preferencesController.loadPreferences()
    var isReady = SimpleBooleanProperty(false)
    var courseName = SimpleStringProperty()
    var requestedNumControls = SimpleIntegerProperty()
    var requestedDistance = SimpleDoubleProperty()

    private val legViewModel: ScoredLegModel by inject()

    init {
        preferencesViewModel.item = preferences
    }

    fun initializeGH(properties: Properties) {
        streetO = StreetO(
            properties.getProperty("pbfFile"),
            properties.getProperty("graphDir"),
            preferences
        )
        streetO.registerSniffer(CourseGenerationSniffer)
        isReady.value = true
    }

    val dataBounds: BBox
        get() = streetO.bounds

    fun loadCourse(file: File) {
        val course = if (file.extension == "gpx") {
            streetO.importer.buildFromGPX(file.path)
        } else {
            streetO.importer.buildFromKml(file.inputStream())
        }
        requestedDistance.value = course.requestedDistance
        requestedNumControls.value = course.requestedNumControls
        initialiseCourse(course.controls)
        scoreControls()
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
            val distance = routeChoices.minOf { l -> l.distance }
            ScoredLeg(startControl, endControl, distance, pointLists)
        }.toList()
        legList.clear()
        legList.addAll(legs)
        controlList.clear()
        controls.map(this::toControl).forEach(controlList::add)
    }

    fun toControl(start: ControlSite) =
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

    fun selectLegTo(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            val leg = CourseLeg(controlList[idx - 1], control)
            legViewModel.item = legList.first { it.start == leg.start && it.end == leg.end }
        }
    }

    fun selectLegFrom(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            val leg = CourseLeg(control, controlList[idx + 1])
            legViewModel.item = legList.first { it.start == leg.start && it.end == leg.end }
        }
    }

    fun generateFromControls() {
        val initial = mutableListOf(first(controlSiteList), last(controlSiteList))
        runAsync {
            streetO.generateCourse(requestedDistance.value, requestedNumControls.value, initial)
        } ui { maybeCourse ->
            if (maybeCourse.isPresent) {
                initialiseCourse(maybeCourse.get())
                scoreControls()

            }
        }
    }

    fun getControlAt(point: Point, res: Double): Control? {
        return controlList.find { c -> c.lat.nearly(point.lat, res) && c.lon.nearly(point.lon, res) }
    }

    fun Double.nearly(other: Double, res: Double) = abs(this - other) < 0.00020 * res

    fun moveControl(num: String, lat: Double, lon: Double) {
        with(controlSiteList.find { it.number == num }) {
            if (this != null) {
                this.location.lat = lat
                this.location.lon = lon
                initialiseCourse(controlSiteList.toMutableList())
            }
        }
    }

    fun scoreControls() {
        runAsync {
            val sites = controlList.map { streetO.findNearestControlSiteTo(it.lat, it.lon).get() }
            streetO.score(sites)
        } ui { details ->
            val legDetails = details.legDetails
            details.legScores.mapIndexed { index, score ->
                //ScoredLeg(legList[index].start, legList[index].end, score, legDetails[index])
                val updated = legList[index]
                updated.reScore(score, legDetails[index])
                legList[index] = updated
                println("$index, $score")
            }
            println(details.toString())
        }
    }

    fun flushPreferences(preferences: ObservablePreferences) {
        preferencesController.flushPreferences(preferences)
        streetO.setPreferences(preferences)
    }
}