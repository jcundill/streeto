package org.streeto.ui

import com.graphhopper.util.shapes.GHPoint
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.streeto.ControlSite
import tornadofx.*

val never get() = BooleanExpression.booleanExpression(false.toProperty())


open class Point(lat: Double?, lon: Double?) {
    val latProperty = SimpleDoubleProperty(lat ?: 0.0)
    var lat by latProperty

    val lonProperty = SimpleDoubleProperty(lon ?: 0.0)
    var lon by lonProperty

    override fun toString(): String {
        return "[$lat, $lon]"
    }
}

class PointList(var points: List<Point>)

open class CourseLeg(start: Control, end: Control) {
    val startProperty = SimpleObjectProperty(start)
    val endProperty = SimpleObjectProperty(end)

    var start by startProperty
    var end by endProperty
}

class ScoredLeg(
    start: Control, end: Control, length: Double,
    routeChoice: List<PointList> = listOf(),
    legScore: Double = 0.0,
    scoreDetails: List<Double> = listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
) : CourseLeg(start, end) {
    private var details = scoreDetails
    private var score = legScore

    val overallScoreProperty = SimpleDoubleProperty(score)
    val lengthScoreProperty = SimpleDoubleProperty(details[0])
    val routeChoiceScoreProperty = SimpleDoubleProperty(details[4])
    val complexityScoreProperty = SimpleDoubleProperty(details[1])
    val beenHereBeforeScoreProperty = SimpleDoubleProperty(details[3])
    val comesTooCloseScoreProperty = SimpleDoubleProperty(details[6])
    val dogLegScoreProperty = SimpleDoubleProperty(details[5])
    val placementScoreProperty = SimpleDoubleProperty(details[2])
    val routeChoiceProperty = SimpleObjectProperty(routeChoice)
    val lengthProperty = SimpleDoubleProperty(length)

    val overallScore by overallScoreProperty
    val lengthScore by lengthScoreProperty
    val routeChoiceScore by routeChoiceScoreProperty
    val complexityScore by complexityScoreProperty
    val beenHereBeforeScore by beenHereBeforeScoreProperty
    val comesTooCloseScore by comesTooCloseScoreProperty
    val dogLegScore by dogLegScoreProperty
    val placementScore by placementScoreProperty
    val routeChoice by routeChoiceProperty
    val length by lengthProperty

    fun reScore(overall: Double, details: List<Double>) {
        overallScoreProperty.value = overall
        lengthScoreProperty.value = details[0]
        routeChoiceScoreProperty.value = details[4]
        complexityScoreProperty.value = details[1]
        beenHereBeforeScoreProperty.value = details[3]
        comesTooCloseScoreProperty.value = details[6]
        dogLegScoreProperty.value = details[5]
        placementScoreProperty.value = details[2]
    }
}

class ScoredLegModel : ItemViewModel<ScoredLeg>() {
    val start = bind(ScoredLeg::startProperty)
    val end = bind(ScoredLeg::endProperty)
    val length = bind(ScoredLeg::lengthProperty)
    val overallScore = bind(ScoredLeg::overallScoreProperty)
    val lengthScore = bind(ScoredLeg::lengthScoreProperty)
    val routeChoiceScore = bind(ScoredLeg::routeChoiceScoreProperty)
    val complexityScore = bind(ScoredLeg::complexityScoreProperty)
    val beeHereBeforeScore = bind(ScoredLeg::beenHereBeforeScoreProperty)
    val comesTooCloseScore = bind(ScoredLeg::comesTooCloseScoreProperty)
    val dogLegScore = bind(ScoredLeg::dogLegScoreProperty)
    val placementScore = bind(ScoredLeg::placementScoreProperty)
}

class ControlViewModel : ItemViewModel<Control>() {
    val number = bind(Control::numberProperty)
    val description = bind(Control::descriptionProperty)
    val lat = bind(Control::latProperty)
    val lon = bind(Control::lonProperty)
}

fun ControlSite.toControl(): Control {
    return Control(this.number, this.description, this.location.lat, this.location.lon)
}

class Control(number: String?, description: String?, lat: Double?, lon: Double?) : Point(lat, lon) {
    val numberProperty = SimpleStringProperty(number ?: "")
    var number by numberProperty

    val descriptionProperty = SimpleStringProperty(description ?: "")
    var description by descriptionProperty

    fun toControlSite(): ControlSite {
        val site = ControlSite(GHPoint(lat, lon), description)
        site.number = number
        return site
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Control

        if (number != other.number) return false
        if (description != other.description) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        return result
    }

    override fun toString(): String {
        return "number: $number, description: $description, position: ${super.toString()}"
    }
}

