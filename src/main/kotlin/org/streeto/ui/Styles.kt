package org.streeto.ui

import javafx.scene.paint.Paint
import tornadofx.*

class Styles : Stylesheet() {

    init {
        // No styles yet
        val white = Paint.valueOf("#ffffff")
        tableView {
            backgroundColor += c("#ffffff")
            borderColor += box(white)
        }
        form {
            backgroundColor += c("#ffffff")
            borderColor += box(white)
        }
        progressIndicator {
            maxWidth = 100.px
            maxHeight = 100.px
            backgroundColor = multi(white)
            borderImageWidth = box(0.0.px)
        }
    }
}
