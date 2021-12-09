package org.streeto.ui

import com.graphhopper.ResponsePath
import com.graphhopper.util.shapes.BBox
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import org.streeto.ControlSite
import org.streeto.Course
import org.streeto.StreetO
import org.streeto.gpx.GpxFacade
import org.streeto.kml.KmlWriter
import org.streeto.mapping.PaperSize
import org.streeto.utils.CollectionHelpers.*
import org.streeto.utils.DistUtils.dist
import tornadofx.*
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.streams.toList


class CourseController : Controller() {
    var controlList = SortedFilteredList<Control>()
    var legList = SortedFilteredList<ScoredLeg>()

    private val preferencesController: PreferencesHandler by inject()
    private val preferencesViewModel: PreferencesViewModel by inject()
    private val progressViewModel: GenerationProgressViewModel by inject()
    private val legViewModel: ScoredLegModel by inject()
    private val courseDetailsViewModel: CourseDetailsViewModel by inject()
    private val controlViewModel: ControlViewModel by inject()

    val sniffer = CourseGenerationSniffer()

    private var streetO: StreetO by singleAssign()
    val preferences = preferencesController.loadPreferences()
    var isReady = SimpleBooleanProperty(false)
    var courseName = SimpleStringProperty("streeto")
    var requestedDistance = SimpleDoubleProperty(8000.0)

    init {
        preferencesViewModel.item = preferences
        progressViewModel.item = sniffer
    }

    fun initializeGH(properties: Properties) {
        streetO = StreetO(
            properties.getProperty("pbfFile"),
            properties.getProperty("graphDir"),
            preferences
        )
        streetO.registerSniffer(sniffer)
        isReady.value = true
    }

    val dataBounds: BBox
        get() = streetO.bounds

    fun loadCourse(file: File): Course {
        val course = if (file.extension == "gpx") {
            streetO.importer.buildFromGPX(file.path)
        } else {
            streetO.importer.buildFromKml(file.inputStream())
        }
        requestedDistance.value = course.requestedDistance
        initialiseCourse(course.controls)
        analyseCourse()
        return course
    }

    private fun generateFrom(
        requestedDistance: Double,
        numControls: Int,
        initial: List<ControlSite>
    ): Optional<List<ControlSite>> {
        val maybeSites = streetO.generateCourse(requestedDistance, numControls, initial)
        if (maybeSites.isPresent) {
            val sites = maybeSites.get()
            initialiseCourse(sites)
            analyseCourse()
        }
        return maybeSites
    }

    private fun analyseCourse() {
        generateLegs()
        scoreControls()
        updateViewModel()
    }

    fun generateFromControls(): Optional<List<ControlSite>> {
        return generateFrom(requestedDistance.value, controlList.size - 2, controlList.items.map { it.toControlSite() })
    }


    fun seedFromControls(): Optional<List<ControlSite>> {
        return generateFrom(
            requestedDistance.value,
            getNumberOfControls(),
            controlList.items.map { it.toControlSite() })
    }

    fun newCourse() {
        initialiseCourse(listOf())
    }


    private fun updateViewModel() {
        val sites = controlList.items.map(Control::toControlSite)
        courseDetailsViewModel.name.value = courseName.value
        courseDetailsViewModel.numControls.value = controlList.size - 2
        courseDetailsViewModel.bestDistance.value = streetO.routeControls(sites).distance
        courseDetailsViewModel.crowFliesDistance.value = windowed(sites, 2)
            .map { dist(it[0].location, it[1].location) }.toList().sum()
        courseDetailsViewModel.mapScaleA3.value = streetO.getMapBoxFor(sites, PaperSize.A3).scale
        courseDetailsViewModel.mapScaleA4.value = streetO.getMapBoxFor(sites, PaperSize.A4).scale
        courseDetailsViewModel.mapOrientation.value = streetO.getMapBoxFor(sites, PaperSize.A4).isLandscape
    }

    private fun initialiseCourse(controls: List<ControlSite>) {
        controlList.clear()
        controlList.addAll(controls.map(ControlSite::toControl))
    }

    fun scoreControls() {
        val sites = controlList.items.map(Control::toControlSite)
        val details = streetO.score(sites)
        courseDetailsViewModel.overallScore.value = details.overallScore
        val legDetails = details.legDetails
        details.legScores.mapIndexed { index, score ->
            val updated = legList[index]
            updated.reScore(score, legDetails[index])
            legList[index] = updated
        }
    }

    fun generateLegs() {
        legList.clear()
        val legs = windowed(controlList.items, 2).map {
            val start = first(it)
            val end = last(it)
            val routeChoices = streetO.getLegRoutes(start.toControlSite(), end.toControlSite())
            val pointLists = routeChoices.map { path ->
                val points = path.points.map { p -> Point(p.lat, p.lon) }
                PointList(points)
            }
            val distance = routeChoices.minOf { l -> l.distance }
            ScoredLeg(start, end, distance, pointLists)
        }.toList()
        legs.forEach(legList::add)
        println("Generated ${legList.size} legs")
    }

    fun getRoute(): List<Point> {
        val path: ResponsePath = streetO.routeControls(controlList.items.map { c -> c.toControlSite() })
        return iterableAsStream(path.points)
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

    private fun getNumberOfControls(): Int {
        return (requestedDistance.value / preferencesViewModel.avgLegDistance.value).roundToInt()
    }

    fun getControlAt(point: Point, res: Double): Control? {
        return controlList.find { c -> c.lat.nearly(point.lat, res) && c.lon.nearly(point.lon, res) }
    }

    fun Double.nearly(other: Double, res: Double) = abs(this - other) < 0.00020 * res

    fun moveControl(num: String, lat: Double, lon: Double) {
        val site = streetO.findNearestControlSiteTo(lat, lon)
        if (site.isPresent) {
            with(controlList.find { it.number == num }) {
                if (this != null) {
                    this.lat = site.get().location.lat
                    this.lon = site.get().location.lon
                    initialiseCourse(controlList.map { c -> c.toControlSite() }.toMutableList())
                    analyseCourse()
                    selectLegFrom(this)
                }
            }
        }
    }

    fun flushPreferences(preferences: ObservablePreferences) {
        preferencesController.flushPreferences(preferences)
        streetO.setPreferences(preferences)
    }

    fun saveAs(file: File) {
        val sites = controlList.map(Control::toControlSite)
        val path = streetO.routeControls(sites)
        if (file.extension == "gpx") {
            GpxFacade.writeCourse(file, path, sites)
        } else {
            file.writeText(KmlWriter().generate(sites, courseName.value))
        }
    }

    fun removeNumberedControls() {
        val startFinish = listOf(first(controlList), last(controlList))
        legList.clear()
        controlList.clear()
        controlList.addAll(startFinish)
        analyseCourse()
    }

    fun removeAllControls() {
        legList.clear()
        controlList.clear()
    }

    fun setStartAt(point: Point): Boolean {
        var success = true
        val site = streetO.findNearestControlSiteTo(point.lat, point.lon)
        if (site.isEmpty) {
            success = false
        } else if (controlList.isEmpty()) {
            val start = site.get().toControl()
            start.number = "S1"
            val finish = site.get().toControl()
            finish.number = "F1"
            controlList.add(start)
            controlList.add(finish)
            initialiseCourse(controlList.map(Control::toControlSite))
            analyseCourse()
        } else {
            val start = site.get().toControl()
            start.number = "S1"
            controlList[0] = start
        }
        return success
    }

    fun setFinishAt(point: Point): Boolean {
        var success = true
        val site = streetO.findNearestControlSiteTo(point.lat, point.lon)
        if (site.isEmpty) {
            success = false
        } else if (controlList.isEmpty()) {
            val start = site.get().toControl()
            start.number = "S1"
            val finish = site.get().toControl()
            finish.number = "F1"
            controlList.add(start)
            controlList.add(finish)
            initialiseCourse(controlList.map(Control::toControlSite))
            analyseCourse()
        } else {
            val finish = site.get().toControl()
            finish.number = "F1"
            controlList[controlList.size - 1] = finish
        }
        return success
    }


    fun generatePDF(directory: File) {
        val sites = controlList.map(Control::toControlSite)
        streetO.writeMap(sites, courseName.value, directory)
    }

    fun generateMapRunFiles(directory: File) {
        val sites = controlList.map(Control::toControlSite)
        streetO.writeMapRunFiles(sites, courseName.value, directory)
    }

    fun splitLegAfterSelected() {
        val currIndex = controlList.indexOfFirst { it.number == controlViewModel.number.value }
        splitLeg(currIndex, currIndex + 1)
    }

    fun splitLegBeforeSelected() {
        val currIndex = controlList.indexOfFirst { it.number == controlViewModel.number.value }
        splitLeg(currIndex - 1, currIndex)
    }

    fun removeSelectedControl() {
        val currIndex = controlList.indexOfFirst { it.number == controlViewModel.number.value }
        removeControl(currIndex)
    }

    private fun removeControl(currIndex: Int) {
        controlList.removeAt(currIndex)
        renumberControls()
        analyseCourse()
    }

    private fun splitLeg(startIndex: Int, endIndex: Int) {
        val start = controlList[startIndex]
        val end = controlList[endIndex]
        val newSite = streetO.findNearestControlSiteTo(avg(start.lat, end.lat), avg(start.lon, end.lon))
        if (newSite.isPresent) {
            val newControl = newSite.get().toControl()
            controlList.add(startIndex + 1, newControl)
            renumberControls()
            analyseCourse()
        }
    }

    private fun avg(a: Double, b: Double): Double = (a + b) / 2

    private fun renumberControls() {
        controlList.drop(1).dropLast(1).forEachIndexed { index, control ->
            control.number = (index + 1).toString()
        }
    }

    fun reverseCourse() {
        controlList.reverse()
        first(controlList).number = "S1"
        last(controlList).number = "F1"
        renumberControls()
        analyseCourse()
    }
}