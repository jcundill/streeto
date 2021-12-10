package org.streeto.ui.evolution


import javafx.geometry.Pos
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.util.StringConverter
import org.streeto.ui.preferences.PreferencesViewModel
import tornadofx.*
import java.util.*
import kotlin.math.max

class GenerationProgressView : View("Course Evolution Progress") {

    val model: GenerationProgressViewModel by inject()
    val prefs: PreferencesViewModel by inject()
    var starTime: Long = 0L

    lateinit var line: XYChart.Series<Number, Number>

    override fun onBeforeShow() {
        line.data.clear()
        model.fitness.onChange { newValue ->
            if (newValue?.generation == 1L) {
                starTime = Date().time
            }
            if (newValue != null) { // && newValue.generation % 20 == 1L) {
                line.data.add(XYChart.Data(calcProgress(newValue.generation), newValue.fitness))
            }
        }
    }

    private fun calcProgress(generation: Long): Double {
        val expiredTime = (Date().time - starTime) / 1000.0  // num secs
        val expiredRatio = expiredTime / prefs.maxExecutionTime.value

        val genRatio = generation.toDouble() / prefs.maxGenerations.value.toDouble()

        return max(expiredRatio, genRatio) * 100.0

    }

    override val root = vbox {
        hbox {
            vbox {
                label("Generating Initial Population ...") {
                    hiddenWhen(model.started)
                }
                label(model.fitness, converter = ProgressConverter()) {
                    visibleWhen(model.started)
                }
            }
            button("Stop Now") {
                action { model.finished.value = true }
                enableWhen(model.started)
                alignment = Pos.CENTER_RIGHT
            }
        }
        label("Generation Complete") {
            visibleWhen(model.finished)
        }

        linechart("Evolution", NumberAxis(0.0, 100.0, 10.0), NumberAxis(0.0, 1.0, 0.1)) {
            visibleWhen(model.started)
            createSymbols = false
            line = series("Fitness")
        }
    }

    class ProgressConverter : StringConverter<Progress>() {
        override fun toString(value: Progress): String {
            return "${value.generation}: ${value.fitness}"
        }

        override fun fromString(p0: String?): Progress {
            return Progress(0, 0.0)
        }

    }
}
