package lab.mars.rl.algo.func_approx.prediction

import javafx.application.Application
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SimpleCoarseCoding
import lab.mars.rl.problem.SquareWave
import lab.mars.rl.problem.WaveState
import lab.mars.rl.util.format
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.ui.*
import org.junit.Test

class `Coarse Coding` {
  @Test
  fun `Coarse Coding`() {
    val alpha = 0.2
    val numOfSamples = listOf(10, 40, 160, 2560, 10240)
    val featureWidths = listOf(0.2, .4, 1.0)
    for (numOfSample in numOfSamples) {
      val chart = chart("$numOfSample samples", "state", "value")
      for (featureWidth in featureWidths) {
        val line = line("feature width: ${featureWidth.format(1)}")
        val feature = SimpleCoarseCoding(featureWidth,
                                         SquareWave.domain, 50) { (s) -> (s as WaveState).x }
        val func = LinearFunc(feature)
        repeat(numOfSample) {
          val (s, y) = SquareWave.sample()
          func.w += alpha / feature.features.sumBy { if (it.contains(feature.conv(arrayOf(s)))) 1 else 0 } * (y - func(s)) * func.`▽`(s)
        }
        for (i in 0 until SquareWave.maxResolution) {
          val s = WaveState(i * 2.0 / SquareWave.maxResolution)
          val y = func(s)
          line[i * 2.0 / SquareWave.maxResolution] = y
        }
        chart += line
      }
      D2DChart.charts += chart
    }
    Application.launch(ChartApp::class.java)
  }
}