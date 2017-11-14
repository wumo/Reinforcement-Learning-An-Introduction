package lab.mars.rl.util.ui

import javafx.scene.chart.NumberAxis
import tornadofx.*
import java.util.concurrent.ConcurrentLinkedQueue

class Chart : App(ChartView::class)
class ChartView : View() {
    companion object {
        val lines = ConcurrentLinkedQueue<Pair<String, HashMap<Number, Number>>>()
    }

    override val root = group {
        linechart("V", NumberAxis(), NumberAxis()) {
            for ((description, line) in lines) {
                series(description) {
                    for ((k, v) in line)
                        data(k, v)
                }
            }
            createSymbols = false
        }
    }
}

