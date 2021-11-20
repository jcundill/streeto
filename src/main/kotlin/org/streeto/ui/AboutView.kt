package org.streeto.ui

import tornadofx.*

class AboutView : View("About StreetO") {
    override val root = vbox {
        label("Hello world")
        button("Ok") {
            isDefaultButton = true
        }.action {
            close()
        }
    }
}
