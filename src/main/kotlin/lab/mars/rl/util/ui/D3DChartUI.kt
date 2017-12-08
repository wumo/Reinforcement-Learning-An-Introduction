@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.util.ui

import com.orsoncharts.Chart3DFactory
import com.orsoncharts.Range
import com.orsoncharts.fx.Chart3DViewer
import com.orsoncharts.graphics3d.Dimension3D
import com.orsoncharts.legend.LegendAnchor
import com.orsoncharts.plot.XYZPlot
import com.orsoncharts.renderer.RainbowScale
import com.orsoncharts.renderer.xyz.SurfaceRenderer
import com.orsoncharts.util.Orientation
import javafx.application.Application
import javafx.scene.*
import javafx.scene.layout.FlowPane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import lab.mars.rl.problem.MountainCar
import lab.mars.rl.util.ui.D3DChartUI.Companion.charts
import lab.mars.rl.util.ui.D3DChartUI.D3DChart
import tornadofx.plusAssign
import java.lang.Math.sin

class D3DChartUI: Application() {
  companion object {
    val charts = mutableListOf<D3DChart>()
    var title: String = ""
  }
  
  class D3DChart(val title: String, val xAxisLabel: String, val yAxisLabel: String, val zAxisLabel: String,
                 val xSample: Int, val ySample: Int,
                 val xRange: ClosedRange<Double>, val yRange: ClosedRange<Double>,
                 val zRange: ClosedRange<Double>,
                 val width: Double = 10.0,
                 val height: Double = 10.0,
                 val depth: Double = 5.0,
                 val value: (Double, Double) -> Double)
  
  /**
   * Creates a surface chart for the demo.
   *
   * @return A surface chart.
   */
  private fun createChart(c: D3DChart): SubScene {
    
    val chart = Chart3DFactory.createSurfaceChart(
        "",
        c.title,
        c.value, c.xAxisLabel, c.zAxisLabel, c.yAxisLabel)
    
    val plot = chart.plot as XYZPlot
    plot.dimensions = Dimension3D(c.width, c.depth, c.height)
    val xAxis = plot.xAxis
    xAxis.setRange(c.xRange.start, c.xRange.endInclusive)
    val zAxis = plot.zAxis
    zAxis.setRange(c.yRange.start, c.yRange.endInclusive)
    val renderer = plot.renderer as SurfaceRenderer
    renderer.xSamples = c.xSample
    renderer.zSamples = c.ySample
    renderer.drawFaceOutlines = false
    renderer.colorScale = RainbowScale(Range(c.zRange.start, c.zRange.endInclusive))
    chart.setLegendPosition(LegendAnchor.BOTTOM_RIGHT, Orientation.VERTICAL)
    return SubScene(
        StackPane(Chart3DViewer(chart)),
        400.0, 400.0,
        true,
        SceneAntialiasing.BALANCED
    )
  }
  
  override fun start(stage: Stage?) {
    val flowPane = FlowPane()
    for (chart in charts)
      flowPane += createChart(chart)
    
    val scene = Scene(flowPane, 800.0, 800.0)
    stage!!.scene = scene
    stage.title = title
    stage.show()
  }
}

fun main(args: Array<String>) {
  val chart = D3DChart("y = sin(x^2 + z^2)",
                       "X", "Z", "Y",
                       40, 40,
                       MountainCar.POSITION_MIN..MountainCar.POSITION_MAX,
                       MountainCar.VELOCITY_MIN..MountainCar.VELOCITY_MAX, -1.0..1.0, 10.0, 10.0, 5.0
  ) { x, y ->
    assert(x in MountainCar.POSITION_MIN..MountainCar.POSITION_MAX)
    assert(y in MountainCar.VELOCITY_MIN..MountainCar.VELOCITY_MAX)
    println("$x,$y"); sin(x * x + y * y)
  }
  charts += chart
  Application.launch(D3DChartUI::class.java)
}