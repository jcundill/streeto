package org.streeto.ui.evolution


import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.util.StringConverter
import org.streeto.ui.StreetOView
import org.streeto.ui.preferences.PreferencesViewModel
import tornadofx.*
import java.util.*
import kotlin.math.max

class GenerationProgressView : StreetOView("Course Evolution Progress") {

    val model: GenerationProgressViewModel by inject()
    val prefs: PreferencesViewModel by inject()
    var startTime: Long = 0L

    lateinit var line: XYChart.Series<Number, Number>

    override fun onBeforeShow() {
        super.onBeforeShow()
        model.fitness.onChange { newValue ->
            if (newValue?.generation == 1L) {
                startTime = Date().time
            }
            if (newValue != null) {
                line.data.add(XYChart.Data(calcProgress(newValue.generation), newValue.fitness))
            }
        }
    }

    private fun calcProgress(generation: Long): Double {
        val expiredTime = (Date().time - startTime) / 1000.0  // num secs
        val expiredRatio = expiredTime / prefs.maxExecutionTime.value

        val genRatio = generation.toDouble() / prefs.maxGenerations.value.toDouble()

        return max(expiredRatio, genRatio) * 100.0

    }

    override fun onUndock() {
        model.finished.value = true
        super.onUndock()
    }

    override val root = vbox {
        prefWidth = 400.0
        prefHeight = 400.0
        model.started.onChange {
            if (it) {
                replaceChildren(evolveCourseView())
            } else {
                replaceChildren(buildPopulationView())
            }
        }
        add(buildPopulationView())
    }

    private fun evolveCourseView(): Node {
        return vbox {
            label("Generation Complete") {
                visibleWhen(model.finished)
            }
            linechart("Course Fitness", NumberAxis(0.0, 100.0, 10.0), NumberAxis(0.0, 1.0, 0.1)) {
                visibleWhen(model.started)
                createSymbols = false
                line = series("Fitness") {
                    isLegendVisible = false
                }
            }
            line.data.clear()
            hbox {
                paddingAll = 10.0
                button("Stop Evolution") {
                    action { model.finished.value = true }
                    enableWhen(model.started.and(model.finished.not()))
                }
                label(model.fitness, converter = ProgressConverter()) {
                    paddingLeft = 40.0
                    paddingTop = 5.0
                }
            }
        }
    }

    private fun buildPopulationView(): Node {
        return borderpane {
            top {
                label("Generating Initial Population ...")
            }
            center {
                minHeight = 350.0
                progressindicator {
                    style {
                        alignment = Pos.CENTER
                        vAlignment = VPos.CENTER
                        prefHeight = 200.px
                        prefWidth = 200.px
                    }
                }
            }
        }
    }

    class ProgressConverter : StringConverter<Progress>() {
        override fun toString(value: Progress): String {
            return "Generation: ${value.generation}, Fitness: ${"%.3f".format(value.fitness)}"
        }

        override fun fromString(p0: String?): Progress {
            return Progress(0, 0.0)
        }
    }
}
