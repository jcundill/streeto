package org.streeto.ui

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.streeto.ControlSite
import org.streeto.StreetOSniffer

data class Progress(val generation: Long, val fitness: Double)

class CourseGenerationSniffer : StreetOSniffer() {
    val startedGAProperty = SimpleBooleanProperty(false)
    val fitnessProperty = SimpleObjectProperty(Progress(0, 0.0))
    val completedProperty = SimpleBooleanProperty(false)

    fun reset() {
        startedGAProperty.value = false
        fitnessProperty.value = Progress(0, 0.0)
        completedProperty.value = false
    }

    override fun accept(generation: Long, fitness: Double, controls: MutableList<ControlSite>?) {
        Platform.runLater {
            startedGAProperty.value = true
            fitnessProperty.value = Progress(generation, fitness)
        }
    }

    override fun acceptStatistics(details: String?) {
        super.acceptStatistics(details)
        completedProperty.value = true
    }
}