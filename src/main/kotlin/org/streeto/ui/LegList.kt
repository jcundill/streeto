package org.streeto.ui

import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class LegController : Controller() {
    private val legList = mutableListOf<CourseLeg>()
    private var current : CourseLeg? = null

    val currentLeg: CourseLeg?
        get() {
            return current
        }
}

class LegList : View("Legs") {

     override val root = vbox {

    }
}
