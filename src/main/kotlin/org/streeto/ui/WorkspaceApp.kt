package org.streeto.ui

import com.sothawo.mapjfx.MapView
import javafx.application.Application
import javafx.event.EventTarget
import javafx.scene.Scene
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import org.streeto.StreetO
import tornadofx.*

/**
 * Created by miguelius on 04/09/2017.
 */

fun EventTarget.jfxMap(op: MapView.() -> Unit) = MapView().attachTo(this, op)

val streeto = StreetO(
    "/home/jon/code/streeto/extracts/greater-london-latest.osm.pbf",
    "/home/jon/code/streeto/osm_data/grph_greater-london-latest"
)

class WorkspaceApp : App() {
    override val primaryView = StreetOWorkspace::class
    override fun onBeforeShow(view: UIComponent) {
        val scene = view.properties["tornadofx.scene"] as Scene
        val jMetro = JMetro(Style.LIGHT)
        jMetro.scene = scene
        super.onBeforeShow(view)
    }

    init {
        importStylesheet(Styles::class)
     }

}

fun main(args: Array<String>) {
    Application.launch(WorkspaceApp::class.java, *args)
}
