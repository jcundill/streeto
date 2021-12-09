package org.streeto.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class CourseDetailsViewModel : ViewModel() {

    val mapOrientation = SimpleBooleanProperty(false)
    val mapScaleA3 = SimpleDoubleProperty(0.0)
    val mapScaleA4 = SimpleDoubleProperty(0.0)
    val overallScore = SimpleDoubleProperty(0.0)
    val crowFliesDistance = SimpleDoubleProperty(0.0)
    val bestDistance = SimpleDoubleProperty(0.0)
    val numControls = SimpleIntegerProperty(0)
    val name = SimpleStringProperty("")
}
