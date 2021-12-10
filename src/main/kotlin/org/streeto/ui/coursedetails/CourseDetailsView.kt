package org.streeto.ui.coursedetails

import javafx.util.StringConverter
import org.streeto.mapping.PaperSize
import org.streeto.ui.preferences.PreferencesViewModel
import org.streeto.ui.never
import tornadofx.*

class CourseDetailsView : View("Course Details") {
    private val model: CourseDetailsViewModel by inject()
    private val preferences: PreferencesViewModel by inject()

    override val root = vbox {
        form {
            fieldset {
                fieldset("Details") {
                    field("Name") {
                        textfield(model.name).isEditable = false
                    }
                    field("Overall Score") {
                        textfield(model.overallScore).isEditable = false
                    }
                    field("Number Of Controls") {
                        textfield(model.numControls).isEditable = false
                    }
                    field("Best Route Distance") {
                        textfield(model.bestDistance).isEditable = false
                    }
                    field("Crow Flies Distance") {
                        textfield(model.crowFliesDistance).isEditable = false
                    }
                }
                fieldset("Map Details") {
                    field("A3 Map Scale") {
                        textfield(model.mapScaleA3) {
                            isEditable = false
                            closeableWhen { never }
                        }
                    }
                    field("A4 Map Scale") {
                        textfield(model.mapScaleA4).isEditable = false
                    }
                    field("Map Size Preference") {
                        textfield(preferences.paperSize, converter = MapSizeConverter()).isEditable = false
                    }
                    field("Max Scale Preference") {
                        textfield(preferences.maxMapScale).isEditable = false
                    }
                    field("Map Orientation") {
                        textfield(
                            model.mapOrientation,
                            converter = MapOrientationConverter()
                        ).isEditable = false
                    }
                }
            }
        }
    }

    class MapOrientationConverter : StringConverter<Boolean>() {
        override fun toString(p0: Boolean): String = if (p0) "Landscape" else "Portrait"
        override fun fromString(p0: String): Boolean = p0 == "Landscape"
    }

    class MapSizeConverter : StringConverter<PaperSize>() {
        override fun toString(p0: PaperSize?): String = when (p0) {
            null -> "Unknown"
            else -> p0.name
        }

        override fun fromString(p0: String): PaperSize = PaperSize.valueOf(p0)
    }
}
