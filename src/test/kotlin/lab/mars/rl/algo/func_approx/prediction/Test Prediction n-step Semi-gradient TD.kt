package lab.mars.rl.algo.func_approx.prediction

import javafx.application.Application
import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.td.*
import lab.mars.rl.model.impl.func.StateAggregation
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.format
import lab.mars.rl.util.ui.*
import org.junit.Test

class `Test Prediction n-step Semi-gradient TD` {
  @Test
  fun `1000-state Random walk`() {
    val chart = chart("V", "state", "value")
    val (prob, PI) = `1000-state RandomWalk`.make()
    val algo = TemporalDifference(prob, PI)
    algo.episodes = 100000
    val V = algo.`Tabular TD(0)`()
    prob.apply {
      val line = line("TD")
      for (s in states) {
        println("${V[s].format(2)} ")
        line[s[0]] = V[s]
      }
      chart += line
    }

    val algo2 = FunctionApprox(prob, PI)
    algo2.episodes = 100000
    algo2.α = 2e-4
    val func = StateAggregation(`1000-state RandomWalk`.num_states + 2, 10) { (s) -> (s as IndexedState)[0] }
    algo2.`n-step semi-gradient TD`(10, func)
    prob.apply {
      val line = line("n-step semi-gradient TD")
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