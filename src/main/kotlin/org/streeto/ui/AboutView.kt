package org.streeto.ui

import tornadofx.*

class AboutView : View("About StreetO") {
    override val root = vbox {
        maxWidth = 200.0
        label("StreetO is an application to automatically generate Urban Orienteering routes from    Open Street Map data.").isWrapText = true
        label()
        label("StreetO is free to use and licensed under the MIT license.").isWrapText = true
        label()
        label("https://github.com/jcundill/streeto")
        layout()
        label("Copyright (C) 2021 Jon Cundill")

    }
}
