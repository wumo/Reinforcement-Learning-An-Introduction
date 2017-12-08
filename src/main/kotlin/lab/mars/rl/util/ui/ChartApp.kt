package lab.mars.rl.util.ui

import javafx.scene.chart.NumberAxis
import tornadofx.*
import java.util.concurrent.ConcurrentLinkedQueue

class Line(val description: String, val data: MutableMap<Number, Number> = hashMapOf()) {
  operator fun set(x: Number, y: Number) = data.put(x, y)
}

class LineChart(val title: String, val xAxisLabel: String, val yAxisLabel: String,
                val lines: MutableCollection<Line> = ConcurrentLinkedQueue(),
                val xAxisConfig: NumberAxis.() -> Unit = {}, val yAxisConfig: NumberAxis.() -> Unit = {},
                val linesSortor: Array<Line>.() -> Unit = {}) {
  operator fun plusAssign(line: Line) {
    lines += line
  }
}

class D2DChart: View() {
  companion object {
    val charts = mutableListOf<LineChart>()
  }
  
  override val root = stackpane {
    flowpane {
      for (chart in charts)
        chart.apply {
          linechart(title, NumberAxis(), NumberAxis()) {
            (xAxis as NumberAxis).apply {
              isForceZeroInRange = false
              isAutoRanging = true
              label = xAxisLabel
              xAxisConfig(this)
            }
            (yAxis as NumberAxis).apply {
              isForceZeroInRange = false
              isAutoRanging = true
              label = yAxisLabel
              yAxisConfig(this)
            }
            val lines = chart.lines.toTypedArray()
            linesSortor(lines)
            for (line in lines)
              series(line.description) {
                for ((k, v) in line.data)
                  data(k, v)
              }
            createSymbols = false
          }
        }
    }
  }
}

class ChartApp: App(D2DChart::class)
