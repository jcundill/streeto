package org.streeto.ui

import com.graphhopper.ResponsePath
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.streeto.ControlSite
import org.streeto.utils.CollectionHelpers
import org.streeto.utils.CollectionHelpers.first
import org.streeto.utils.CollectionHelpers.last
import tornadofx.*
import java.io.File
import java.util.stream.Collectors
import kotlin.streams.toList

class CourseController : Controller() {
    private var selected  = SimpleObjectProperty<Control?>()
    var selectedLeg = SimpleObjectProperty<RoutedLeg?>()
    private var controlList = SortedFilteredList<Control>()
    private var legList = SortedFilteredList<RoutedLeg>()
    private var controlSiteList = SortedFilteredList<ControlSite>()

    fun loadCourse(file: File) {
        //task {
            val course = if (file.extension == "gpx") {
                streeto.importer.buildFromGPX(file.path)
            } else {
                streeto.importer.buildFromKml(file.inputStream())
            }
            controlSiteList.clear()
            controlSiteList.addAll(course.controls)
            val legs = CollectionHelpers.windowed(course.controls, 2).map{
                val start = first(it)
                val end = last(it)
                val routeChoices = streeto.getLegRoutes(start, end)
                val pointLists = routeChoices.map { path ->
                    val points = path.points.map { p -> Point(p.lat, p.lon) }
                    PointList(points)
                }
                val startControl = toControl(start)
                val endControl =toControl(end)
                RoutedLeg(startControl, endControl, pointLists)
            }.toList()
            legList.clear()
            legList.addAll(legs)
            //return@task course
        //} ui {
            getControls().clear()

            course.controls
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
        val path: ResponsePath = streeto.routeControls(controlSiteList)
        return CollectionHelpers.iterableAsStream(path.points)
            .map{ point -> Point(point.lat, point.lon) }
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

    val selectedControl: SimpleObjectProperty<Control?>
        get() {
            return selected
        }

}