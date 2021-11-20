package org.streeto.ui

import tornadofx.*

class NewCourseView : View("New Course") {
    override val root = vbox {
        form {
            tabpane {
                tab("Screen 1") {
                    fieldset("Personal Info") {
                        field("First Name") {
                            textfield()
                        }
                        field("Last Name") {
                            textfield()
                        }
                        field("Birthday") {
                            datepicker()
                        }
                    }
                }
                tab("Screen 2") {
                    fieldset("Contact") {
                        field("Phone") {
                            textfield()
                        }
                        field("Email") {
                            textfield()
                        }
                    }

                }
            }
            button("Commit") {
                action { println("Wrote to database!") }
            }
        }
    }
}
