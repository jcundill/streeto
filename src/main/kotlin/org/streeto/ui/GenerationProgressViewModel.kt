package org.streeto.ui

import tornadofx.*

class GenerationProgressViewModel : ItemViewModel<CourseGenerationSniffer>() {

    val started = bind(CourseGenerationSniffer::startedGAProperty)
    val finished = bind(CourseGenerationSniffer::completedProperty)
    val fitness = bind(CourseGenerationSniffer::fitnessProperty)

}
