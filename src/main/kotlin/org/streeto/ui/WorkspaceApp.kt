package org.streeto.ui

import javafx.application.Application
import javafx.scene.Scene
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.*


class WorkspaceApp : App() {

    override val primaryView = StreetOWorkspace::class
    override fun onBeforeShow(view: UIComponent) {
        if (!isMac()) {
            val scene = view.properties["tornadofx.scene"] as Scene
            val jMetro = JMetro(Style.LIGHT)
            jMetro.scene = scene
        }

        workspace.primaryStage.icons.add(resources.image("/app-icon.png"))
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
