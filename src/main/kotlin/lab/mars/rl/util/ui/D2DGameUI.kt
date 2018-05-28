package lab.mars.rl.util.ui

import javafx.application.Application
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import lab.mars.rl.util.resource.ResourceLoader
import java.util.concurrent.CyclicBarrier

class D2DGameUI : Application() {
  class ChartDescription(val title: String,
                         val xAxisLabel: String, val yAxisLabel: String,
                         val numSeries: Int = 1,
                         val xForceZeroInRange: Boolean = true,
                         val yForceZeroInRange: Boolean = true) {
    val data = Array(numSeries) { FXCollections.observableArrayList<XYChart.Data<Number, Number>>()!! }
  }
  
  lateinit var canvas: Canvas
  lateinit var primaryStage: Stage
  
  companion object {
    var width = 1000.0
    var height = 800.0
    var canvas_width = 600.0
    var canvas_height = 800.0
    var title = ""
    val charts = FXCollections.observableArrayList<ChartDescription>()!!
    var afterStartup: (GraphicsContext) -> Unit = {}
    lateinit var render: ((GraphicsContext) -> Unit) -> Unit
    
  }
  
  override fun start(ps: Stage?) {
    primaryStage = ps!!
    
    primaryStage.title = title
    val root = FlowPane(Orientation.HORIZONTAL)
    canvas = Canvas(canvas_width, canvas_height)
    root.children.add(canvas)
    for (c in charts) {
      val chart = LineChart(NumberAxis().apply { label = c.xAxisLabel;isForceZeroInRange = c.xForceZeroInRange },
                            NumberAxis().apply { label = c.yAxisLabel;isForceZeroInRange = c.yForceZeroInRange },
                            FXCollections.observableArrayList<XYChart.Series<Number, Number>>().apply {
                              var i = 0
                              for (d in c.data)
                                add(XYChart.Series("${i++}", d))
                            }).apply {
        title = c.title
        createSymbols = false
//        isLegendVisible = false
        animated = false
        stylesheets.add(ResourceLoader.getResource("StockLineChart.css").toExternalForm())
      }
      root.children.add(chart)
    }
    
    primaryStage.scene = Scene(root, width, height)
    primaryStage.show()
    render = this::render
    afterStartup(canvas.graphicsContext2D)
  }
  
  val barrier = CyclicBarrier(2)
  fun render(draw: (GraphicsContext) -> Unit = {}) {
    barrier.reset()
    Platform.runLater {
      val gc = canvas.graphicsContext2D
      draw(gc)
      primaryStage.title = title
      barrier.await()
    }
    barrier.await()
  }
  
}