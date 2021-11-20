package org.streeto.ui

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

open class Point(lat: Double, lon: Double) {
    val latProperty = SimpleDoubleProperty(lat)
    var lat by latProperty

    val lonProperty = SimpleDoubleProperty(lon)
    var lon by lonProperty

}

class PointList(var points: List<Point>)
open class CourseLeg(start: Control, end: Control) {
    val startProperty = SimpleObjectProperty(start)
    val endProperty = SimpleObjectProperty(end)

    var start by startProperty
    var end by endProperty
}

class RoutedLeg(start: Control, end: Control, routeChoice: List<PointList>) : CourseLeg(start, end) {
    val routeChoiceProperty = SimpleObjectProperty(routeChoice)
    val routeChoice by routeChoiceProperty
}

class Control(number: String, description: String, lat: Double, lon: Double) : Point(lat, lon) {

    val numberProperty = SimpleStringProperty(number)
    var number by numberProperty

    val descriptionProperty = SimpleStringProperty(description)
    var description by descriptionProperty

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Control

        if (number != other.number) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}

