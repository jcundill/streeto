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
            maxWidth = 100.px
            maxHeight = 100.px
            backgroundColor = multi(white)
            borderImageWidth = box(0.0.px)
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

        private fun isMac(): Boolean {
            with(System.getProperty("os.name", "generic").lowercase()) {
                return (indexOf("mac") >= 0) || (indexOf("darwin") >= 0)
            }
        }


    }
}
