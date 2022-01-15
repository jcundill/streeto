package org.streeto.ui

import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import tornadofx.*

class StreetOMaskPane(displayText: String = "Loading...", color: Color = Color.WHITE) : BorderPane() {
    init {
        addClass("mask-pane")
        style {
            backgroundColor += c("#000000", 0.5)
            accentColor = Color.ALICEBLUE
            minWidth = 300.px
            minHeight = 300.px
        }

        center = vbox {
            alignment = Pos.CENTER
            progressindicator()
            label {
                style {
                    fontSize = 20.px
                    textFill = color
                }
                text = displayText
            }
        }
    }
}