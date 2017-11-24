package lab.mars.rl.util.ui

import javafx.scene.chart.NumberAxis
import tornadofx.*
import java.util.concurrent.ConcurrentLinkedQueue

class line(val description: String, val data: MutableMap<Number, Number> = hashMapOf()) {
    operator fun set(x: Number, y: Number) = data.put(x, y)
}

class chart(val title: String, val lines: MutableCollection<line> = ConcurrentLinkedQueue()) {
    operator fun plusAssign(line: line) {
        lines += line
    }
}

class ChartView : View() {
    companion object {
        val charts = mutableListOf<chart>()
    }

    override val root = stackpane {
        flowpane {
            for (chart in charts) {
                linechart(chart.title, NumberAxis(), NumberAxis()) {
                    for (line in chart.lines) {
                        series(line.description) {
                            for ((k, v) in line.data)
                                data(k, v)
                        }
                    }
                    createSymbols = false
                }
            }
        }
    }
}

class ChartApp : App(ChartView::class)
