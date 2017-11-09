package lab.mars.rl.util.ui

import javafx.scene.chart.NumberAxis
import tornadofx.*

class Chart : App(ChartView::class)
class ChartView : View() {
    companion object {
        val lines = mutableListOf<HashMap<Number, Number>>()
    }

    override val root = group {
        linechart("V", NumberAxis(), NumberAxis()) {
            for ((i, line) in lines.withIndex()) {
                series("$i") {
                    for ((k, v) in line)
                        data(k, v)
                }
            }
           createSymbols=false
        }
    }
}

