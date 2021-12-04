package org.streeto.ui

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*


class NewCourseView : View("New Course") {
    private val newCourse = object {
        var name = SimpleStringProperty("StreetOCourse")
        var requestedDistance = SimpleDoubleProperty(8000.0)
    }
    private val controller: CourseController by inject()

    override val root = vbox {
        form {
            fieldset {
                field("Course Name") {
                    textfield(newCourse.name)
                }
                field("Course Length") {
                    textfield(newCourse.requestedDistance)
                }
            }
            hbox {
                alignment = Pos.CENTER_RIGHT
                button("Save") {
                    action {
                        controller.courseName.value = newCourse.name.value
                        controller.requestedDistance.value = newCourse.requestedDistance.value
                        this@NewCourseView.close()
                    }
                }
                button("Cancel") {
                    action {
                        this@NewCourseView.close()
                    }
                }
            }
        }
    }
}
