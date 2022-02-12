package org.streeto.ui

import com.graphhopper.ResponsePath
import com.graphhopper.util.shapes.BBox
import com.graphhopper.util.shapes.GHPoint
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.streeto.ControlSite
import org.streeto.Course
import org.streeto.StreetO
import org.streeto.csim.RouteSimilarityFinder
import org.streeto.gpx.GpxFacade
import org.streeto.kml.KmlWriter
import org.streeto.mapping.PaperSize
import org.streeto.osmdata.PbfInfo
import org.streeto.ui.coursedetails.CourseDetailsViewModel
import org.streeto.ui.evolution.CourseGenerationSniffer
import org.streeto.ui.evolution.GenerationProgressViewModel
import org.streeto.ui.osmdata.OsmDataController
import org.streeto.ui.preferences.ObservablePreferences
import org.streeto.ui.preferences.PreferencesHandler
import org.streeto.ui.preferences.PreferencesViewModel
import org.streeto.utils.CollectionHelpers.*
import org.streeto.utils.DistUtils.dist
import tornadofx.Controller
import tornadofx.SortedFilteredList
import tornadofx.reverse
import tornadofx.singleAssign
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.math.abs
import kotlin.math.roundToInt


class CourseController : Controller() {
    var controlList = SortedFilteredList<Control>()
    var legList = SortedFilteredList<ScoredLeg>()
    var route = SortedFilteredList<Control>()

    private val preferencesController: PreferencesHandler by inject()
    private val osmDataController: OsmDataController by inject()
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
    val courseFile = SimpleObjectProperty<File>()
    lateinit var osmDir: String

    private var lastFurniturePosition: GHPoint? = null

    init {
        preferencesViewModel.item = preferences
        progressViewModel.item = sniffer
    }

    fun initializeGH(osmDir: String) {
        this.osmDir = osmDir
        streetO = StreetO(osmDir, preferences)
        streetO.registerSniffer(sniffer)
        isReady.value = true
    }

    val dataBounds: BBox
        get() = streetO.bounds

    fun loadCourse(file: File): Course? {
        return try {
            val course = if (file.extension == "gpx") {
                streetO.importer.buildFromGPX(file.path)
            } else {
                streetO.importer.buildFromKml(file.inputStream())
            }
            requestedDistance.value = course.requestedDistance
            course
        } catch (e: Exception) {
            null
        }
    }

    private fun generateFrom(
        requestedDistance: Double,
        numControls: Int,
        initial: List<ControlSite>
    ): Optional<List<ControlSite>> {
        return streetO.generateCourse(requestedDistance, numControls, initial)
    }

    fun analyseCourse(route: ObservableList<Control> = controlList.items) {
        generateLegs(route)
        scoreControls(route)
        updateViewModel(route)
    }

    fun generateFromControls(): Optional<List<ControlSite>> {
        return generateFrom(
            courseDetailsViewModel.bestDistance.value,
            controlList.size - 2,
            controlList.items.map { it.toControlSite() })
    }


    fun seedFromControls(): Optional<List<ControlSite>> {
        return generateFrom(
            requestedDistance.value,
            getNumberOfControls(),
            controlList.items.map { it.toControlSite() })
    }

    fun seedScatterCourse(distance: Double, totalControls : Int, requiredControls: Int, iterations: Int): Optional<List<ControlSite>> {
        return streetO.generateScatterCourse(distance, totalControls, requiredControls, iterations, controlList.items.map { it.toControlSite() })
    }

    fun updateViewModel(route: ObservableList<Control>) {
        val sites = controlList.items.map(Control::toControlSite)
        val track = route.map { it.toControlSite() }
        courseDetailsViewModel.name.value = courseName.value
        courseDetailsViewModel.numControls.value = controlList.size - 2
        courseDetailsViewModel.requestedDistance.value = requestedDistance.value
        courseDetailsViewModel.distanceTolerance.value = preferences.allowedCourseLengthDeltaProperty.value
        courseDetailsViewModel.bestDistance.value = streetO.routeControls(track).distance
        courseDetailsViewModel.crowFliesDistance.value = windowed(track, 2)
            .map { dist(it[0].location, it[1].location) }.toList().sum()
        courseDetailsViewModel.mapScaleA3.value = streetO.getMapBoxFor(sites, PaperSize.A3)?.scale ?: 20000.0
        courseDetailsViewModel.mapScaleA4.value = streetO.getMapBoxFor(sites, PaperSize.A4)?.scale ?: 20000.0
        courseDetailsViewModel.mapOrientation.value = streetO.getMapBoxFor(sites, PaperSize.A3)?.isLandscape ?: true
    }

    fun initialiseCourse(controls: List<ControlSite>) {
        val snapped = controls.map {
            streetO.findNearestControlSiteTo(it.location.lat, it.location.lon).orElseThrow()
        }
        controlList.clear()
        controlList.addAll(snapped.map(ControlSite::toControl))
        renumberControls()
    }

    fun scoreControls(route: ObservableList<Control>) {
        val sites = route.map(Control::toControlSite)
        val details = streetO.score(sites)
        //TODO: details can be null
        courseDetailsViewModel.overallScore.value = details.overallScore
        val legDetails = details.legDetails
        details.legScores.mapIndexed { index, score ->
            val updated = legList[index]
            updated.reScore(score, legDetails[index])
            legList[index] = updated
        }
    }

    private fun generateLegs(route: ObservableList<Control>) {
        legList.clear()
        val ctrls: List<ControlSite> = route.map(Control::toControlSite)
        val legs = windowed(ctrls, 2).map { it ->
            val start = first(it)
            val end = last(it)
            val routeChoices =
                streetO.getLegRoutes(start, end).sortedBy { c -> c.distance }
            val pointLists = routeChoices.map { path ->
                val points = path.points.map { p -> Point(p.lat, p.lon) }
                PointList(points)
            }
            val distance = routeChoices.minOf { l -> l.distance }
            val choiceDetails = routeChoices.map { l -> getChoiceDetails(routeChoices.first(), l, distance) }
            ScoredLeg(start.toControl(), end.toControl(), distance, pointLists, routeChoiceDetails = choiceDetails)
        }.toList()
        legs.forEach(legList::add)
    }

    private fun getChoiceDetails(best: ResponsePath, path: ResponsePath, bestDistance: Double): RouteChoiceDetails {
        val distance = path.distance
        val ratio = distance / bestDistance
        val csim = RouteSimilarityFinder(preferences).similarity(best, path)
        return RouteChoiceDetails(distance, ratio, csim)
    }

    fun getRoute(): List<Point> {
        val path: ResponsePath = streetO.routeControls(route.items.map { c -> c.toControlSite() })
        return iterableAsStream(path.points)
            .map { point -> Point(point.lat, point.lon) }
            .collect(Collectors.toList())
    }

    fun selectLegTo(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            if (idx >= 0) {
                val leg = CourseLeg(controlList[idx - 1], control)
                legViewModel.item = legList.first { it.start == leg.start && it.end == leg.end }
            }
        }
    }

    fun selectLegFrom(control: Control?) {
        if (control != null) {
            val idx = controlList.indexOf(control)
            if (idx < controlList.size - 1) {
                val leg = CourseLeg(control, controlList[idx + 1])
                legViewModel.item = legList.first { it.start == leg.start && it.end == leg.end }
            }
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

    fun resetPreferences() {
        preferencesViewModel.item = preferencesController.loadPreferences()
    }

    fun flushPreferences(preferences: ObservablePreferences) {
        preferencesController.flushPreferences(preferences)
        streetO.setPreferences(preferences)
    }

    fun saveAs(file: File) {
        courseFile.value = file
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

    private fun setNonNumberedControl(point: Point, function: (Optional<ControlSite>) -> Unit): Boolean {
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
            function(site)
            analyseCourse()
        }
        return success
    }

    fun setStartAt(point: Point): Boolean {
        return setNonNumberedControl(point, this::updateStart)
    }

    private fun updateStart(site: Optional<ControlSite>) {
        val start = site.get().toControl()
        start.number = "S1"
        controlList[0] = start
    }

    fun setFinishAt(point: Point): Boolean {
        return setNonNumberedControl(point, this::updateFinish)
    }

    private fun updateFinish(site: Optional<ControlSite>) {
        val finish = site.get().toControl()
        finish.number = "F1"
        controlList[controlList.size - 1] = finish
    }

    fun generatePDF(directory: File): Boolean {
        return try {
            val sites = controlList.map(Control::toControlSite)
            streetO.writeMap(sites, courseName.value, directory)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generateMapRunFiles(directory: File, name: String): Boolean {
        return try {
            val sites = controlList.map(Control::toControlSite)
            streetO.writeMapRunFiles(sites, name, directory)
            true
        } catch (e: Exception) {
            false
        }
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
        first(controlList).number = "S1"
        last(controlList).number = "F1"
        controlList.drop(1).dropLast(1).forEachIndexed { index, control ->
            control.number = (index + 1).toString()
        }
    }

    fun reverseCourse() {
        controlList.reverse()
        renumberControls()
        analyseCourse()
    }

    fun saveLastLocation(mapCenter: Point, resolution: Double) {
        preferencesController.setLastLocation(mapCenter)
        preferencesController.setLastResolution(resolution)
    }

    fun getLastLocation(): Point {
        return preferencesController.getLastLocation()
    }

    fun getLastResolution(): Double {
        return preferencesController.getLastResolution()
    }

    private fun switchToMapDataFor(position: Point): Boolean {
        val location = GHPoint(position.lat, position.lon)
        if (osmDataController.hasMapDataFor(location)) {
            return streetO.initialiseGHFor(location).isPresent
        }
        return false
    }

    fun loadMapDataAt(position: Point, fetchIfNeeded: Boolean = false): Boolean {
        val point = GHPoint(position.lat, position.lon)
        val loaded = if (fetchIfNeeded) {
            streetO.initialiseGHFor(point).isPresent
        } else {
            switchToMapDataFor(position)
        }
        if (loaded && !hasFurnitureFor(point)) {
            streetO.findFurniture(point)
            lastFurniturePosition = point
        }
        return loaded
    }

    private fun hasFurnitureFor(point: GHPoint): Boolean {
        // if we have furniture for this area, we can use it
        return lastFurniturePosition != null && dist(point, lastFurniturePosition!!) < 1000
    }

    fun hasMapDataFor(location: Point): Boolean {
        return osmDataController.hasMapDataFor(GHPoint(location.lat, location.lon))
    }

    fun isCurrentCourseLengthValid(): Boolean {
        val existingLength = courseDetailsViewModel.bestDistance.value
        val maxAllowedLength = getMaxAllowedCourseLength()
        return existingLength < maxAllowedLength
    }

    fun getMaxAllowedCourseLength(): Double {
        return requestedDistance.value * (1.0 + preferencesViewModel.allowedCourseLengthDelta.value)
    }

    fun setDetailsFrom(file: File) {
        courseFile.value = file
        courseName.value = file.nameWithoutExtension
        requestedDistance.value = courseDetailsViewModel.bestDistance.value.roundToInt().toDouble()
        updateViewModel(controlList.items)
    }

    fun getGeoFabrikExtractFor(position: Point): Optional<PbfInfo> {
        val point = GHPoint(position.lat, position.lon)
        return streetO.getGeoFabrikExtractDetailsFor(point)
    }

    fun runVrp(distance: Double): List<ControlSite> {
        val controlSites = controlList.map { c -> c.toControlSite() }.dropLast(1)
        val route = streetO.runVRP(controlSites, distance, 10000)
        return route
    }
}