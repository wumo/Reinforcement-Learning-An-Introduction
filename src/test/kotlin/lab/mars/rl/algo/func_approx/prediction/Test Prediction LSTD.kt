package lab.mars.rl.algo.func_approx.prediction

import javafx.application.Application
import lab.mars.rl.algo.td.`Tabular TD(0)`
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SimpleTileCoding
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.format
import lab.mars.rl.util.ui.ChartApp
import lab.mars.rl.util.ui.D2DChart
import lab.mars.rl.util.ui.Line
import lab.mars.rl.util.ui.LineChart
import org.apache.commons.math3.util.FastMath.ceil
import org.junit.Test

class `Test Prediction LSTD` {
  @Test
  fun `1000-state RandomWalk`() {
    val chart = LineChart("V", "state", "value")
    val (prob, π) = `1000-state RandomWalk`.make()
    val V = prob.`Tabular TD(0)`(π = π, episodes = 100000, α = 0.1)
    prob.apply {
      val line = Line("TD")
      for (s in states) {
        println("${V[s].format(2)} ")
        line[s[0]] = V[s]
      }
      chart += line
    }
    
    val numOfTilings = 50
    val feature = SimpleTileCoding(numOfTilings,
                                   5,
                                   ceil(`1000-state RandomWalk`.num_states / 5.0).toInt(),
                                   4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
    val func = LinearFunc(feature)
    prob.LSTD(vFunc = func, π = π, ε = 1.0, episodes = 100)
    prob.apply {
      val line = Line("LSTD")
      for (s in states) {
        println("${func(s).format(2)} ")
        line[s[0]] = func(s)
      }
      chart += line
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}