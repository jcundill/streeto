package org.streeto.ui.about

import javafx.geometry.Pos
import org.streeto.ui.StreetOView
import tornadofx.*

class AboutView : StreetOView("About StreetO") {

    private val version: String ;
    init {
        resources.url("/VERSION.txt").openStream().bufferedReader().use {
            version = it.readLine()
        }
        title = "About StreetO - $version"

    }
    override val root = vbox {
        prefWidth = 660.0
        hbox {
            paddingTop = 20.0
            imageview(resources.image("/app-icon.png")) {
                fitHeight = 130.0
                fitWidth = 130.0
                paddingLeft = 20.0
            }
            label("StreetO is an application to automatically generate Urban Orienteering routes from Open Street Map data.") {
                isWrapText = true
                style {
                    paddingLeft = 20.0
                    paddingRight = 20.0
                    paddingVertical = 40.0
                    fontSize = 20.px
                }
            }
        }
        label("StreetO is open source (MIT Licenced) and free to use. You can find the source code on GitHub.") {
            isWrapText = true
            style {
                paddingLeft = 20.0
                paddingRight = 20.0
                paddingVertical = 20.0
                fontSize = 16.px
            }
        }
        hbox {
            alignment = Pos.CENTER
            paddingBottom = 20.0
            hyperlink("https://github.com/jcundill/streeto") {
                paddingLeft = 20.0
                style {
                    fontSize = 16.px
                }
                action {
                    hostServices.showDocument("https://github.com/jcundill/streeto")
                }
            }
        }
        textarea(
            "\nCopyright (c) 2021 Jon Cundill\n" +
                    "\n\n" +
                    "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                    "of this software and associated documentation files (the \"Software\"), to deal\n" +
                    "in the Software without restriction, including without limitation the rights\n" +
                    "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                    "copies of the Software, and to permit persons to whom the Software is\n" +
                    "furnished to do so, subject to the following conditions:\n" +
                    "\n" +
                    "The above copyright notice and this permission notice shall be included in all\n" +
                    "copies or substantial portions of the Software.\n" +
                    "\n" +
                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                    "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                    "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                    "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                    "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                    "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                    "SOFTWARE."
        ) {
            isEditable = false
            prefRowCount = 22
            style {

                fontFamily = "monospace"
                borderColor = multi((box(c("#ffffff", 1.0))))

            }
        }
        vbox {
            paddingAll = 10.0
            hbox {
                hyperlink("Map PDFs courtesy of the Open Orienteering Map project") {
                    action {
                        hostServices.showDocument("https://blog.oomap.co.uk/oom/")
                    }
                }
            }
            hbox {
                hyperlink("Application Icon: מאיר מ at Hebrew Wikipedia, CC BY-SA 3.0") {
                    action {
                        hostServices.showDocument("https://commons.wikimedia.org/w/index.php?curid=10226955")
                    }
                }
            }
        }
    }
}
