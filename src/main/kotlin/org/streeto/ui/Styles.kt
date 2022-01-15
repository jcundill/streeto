package org.streeto.ui

import javafx.scene.paint.Paint
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.*

class Styles : Stylesheet() {

    init {
        val white = Paint.valueOf("#f8f8f8")
        tableView {
            backgroundColor += c("#f8f8f8")
            borderColor += box(white)
        }
        form {
            backgroundColor += c("#f8f8f8")
            borderColor += box(white)
        }
        progressIndicator {
            minWidth = 200.px
            minHeight = 200.px
            maxHeight = 200.px
            maxWidth = 200.px
            borderColor += box(c("#f8f8f8", 0.5))
            backgroundColor += c("#f8f8f8", 0.5)
        }
    }

    companion object {
        fun styleView(view: UIComponent) {
            if (!isMac()) {
                val scene = view.currentStage?.scene
                val jMetro = JMetro(Style.LIGHT)
                jMetro.scene = scene
            }
        }

        fun isMac(): Boolean {
            with(System.getProperty("os.name", "generic").lowercase()) {
                return (indexOf("mac") >= 0) || (indexOf("darwin") >= 0)
            }
        }


    }
}
