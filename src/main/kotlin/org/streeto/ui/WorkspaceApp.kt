package org.streeto.ui

import com.sothawo.mapjfx.MapView
import javafx.application.Application
import javafx.event.EventTarget
import javafx.scene.Scene
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.*


fun EventTarget.jfxMap(op: MapView.() -> Unit) = MapView().attachTo(this, op)

class WorkspaceApp : App() {

    override val primaryView = StreetOWorkspace::class
    override fun onBeforeShow(view: UIComponent) {
        if (!isMac()) {
            val scene = view.properties["tornadofx.scene"] as Scene
            val jMetro = JMetro(Style.LIGHT)
            jMetro.scene = scene
        }
        super.onBeforeShow(view)
    }

    private fun isMac(): Boolean {
        with(System.getProperty("os.name", "generic").lowercase()) {
            return (indexOf("mac") >= 0) || (indexOf("darwin") >= 0)
        }
    }

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
    Application.launch(WorkspaceApp::class.java, *args)
}
