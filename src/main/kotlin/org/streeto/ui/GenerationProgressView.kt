package org.streeto.ui

import tornadofx.*

class GenerationProgressView : View("Course Evolution Progress") {
    override val root = vbox {
        label("Generating Initial Population ...") {
            hiddenWhen(CourseGenerationSniffer.startedGAProperty)
        }
        textfield(CourseGenerationSniffer.generationProperty) {
            visibleWhen(CourseGenerationSniffer.startedGAProperty)

        }
        textfield(CourseGenerationSniffer.fitnessProperty) {
            visibleWhen(CourseGenerationSniffer.startedGAProperty)
        }
    }
}
