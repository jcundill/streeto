package org.streeto.ui

import javafx.application.Application
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.importStylesheet


class WorkspaceApp : App() {
    override val primaryView = StreetOWorkspace::class
    override fun onBeforeShow(view: UIComponent) {
        Styles.styleView(view)
        workspace.primaryStage.icons.add(resources.image("/app-icon.png"))
        super.onBeforeShow(view)
    }

    init {
        importStylesheet(Styles::class)
    }

}

fun main(args: Array<String>) {
    Application.launch(WorkspaceApp::class.java, *args)
}
