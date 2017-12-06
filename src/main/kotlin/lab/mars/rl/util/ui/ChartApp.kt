package lab.mars.rl.util.ui

import javafx.scene.chart.NumberAxis
import tornadofx.*
import java.util.concurrent.ConcurrentLinkedQueue

class line(val description: String, val data: MutableMap<Number, Number> = hashMapOf()) {
  operator fun set(x: Number, y: Number) = data.put(x, y)
}

class chart(val title: String, val xAxisLabel: String, val yAxisLabel: String, val lines: MutableCollection<line> = ConcurrentLinkedQueue()) {
  operator fun plusAssign(line: line) {
    lines += line
  }
}

class D2DChart: View() {
  companion object {
    val charts = mutableListOf<chart>()
  }
  
  override val root = stackpane {
    flowpane {
      for (chart in charts) {
        linechart(chart.title, NumberAxis(), NumberAxis()) {
          (xAxis as NumberAxis).apply {
            isForceZeroInRange = false
            isAutoRanging = true
            label = chart.xAxisLabel
          }
          (yAxis as NumberAxis).apply {
            isForceZeroInRange = false
            isAutoRanging = true
            label = chart.yAxisLabel
          }
          val lines = chart.lines.toTypedArray()
//          lines.sortBy { it.description }
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
