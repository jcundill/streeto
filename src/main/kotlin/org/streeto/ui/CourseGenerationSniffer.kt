package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleLongProperty
import org.streeto.ControlSite
import org.streeto.StreetOSniffer
import tornadofx.*

object CourseGenerationSniffer : StreetOSniffer() {
    val startedGAProperty = SimpleBooleanProperty(false)
    val controller = find<CourseController>()
    val generationProperty = SimpleLongProperty(0L)
    val fitnessProperty = SimpleDoubleProperty(0.0)

    fun reset() {
        startedGAProperty.value = false
        generationProperty.value = 0L
        fitnessProperty.value = 0.0
    }

    override fun accept(generation: Long, fitness: Double, controls: MutableList<ControlSite>?) {
        Platform.runLater {
            startedGAProperty.value = true
            generationProperty.value = generation
            fitnessProperty.value = fitness
        }
    }

    override fun acceptStatistics(details: String?) {
        super.acceptStatistics(details)
    }
}