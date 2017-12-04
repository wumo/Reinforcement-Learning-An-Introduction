package lab.mars.rl.algo.func_approx.prediction

import javafx.application.Application
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.td.TemporalDifference
import lab.mars.rl.algo.td.prediction
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SimpleTileCoding
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.format
import lab.mars.rl.util.ui.*
import org.apache.commons.math3.util.FastMath.ceil
import org.junit.Test

class `Test Prediction LSTD` {
  @Test
  fun `1000-state RandomWalk`() {
    val chart = chart("V", "state", "value")
    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.prediction()
    prob.apply {
      val line = line("TD")
      for (s in states) {
        println("${V[s].format(2)} ")
        line[s[0]] = V[s]
      }
      chart += line
    }

    val algo2 = FunctionApprox(prob, PI)
    algo2.episodes = 100
    val numOfTilings = 50
    val feature = SimpleTileCoding(numOfTilings,
                                   5,
                                   ceil(`1000-state RandomWalk`.num_states / 5.0).toInt(),
                                   4.0) { (s) -> ((s as IndexedState)[0] - 1).toDouble() }
    val func = LinearFunc(feature)
    algo2.LSTD(func, 1.0)
    prob.apply {
      val line = line("LSTD")
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