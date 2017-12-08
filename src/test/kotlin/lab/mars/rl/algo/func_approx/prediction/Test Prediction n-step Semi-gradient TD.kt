package lab.mars.rl.algo.func_approx.prediction

import javafx.application.Application
import lab.mars.rl.algo.td.`Tabular TD(0)`
import lab.mars.rl.model.impl.func.StateAggregation
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.`1000-state RandomWalk`
import lab.mars.rl.util.format
import lab.mars.rl.util.ui.*
import org.junit.Test

class `Test Prediction n-step Semi-gradient TD` {
  @Test
  fun `1000-state Random walk`() {
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
    
    val func = StateAggregation(`1000-state RandomWalk`.num_states + 2,
                                10) { (s) -> (s as IndexedState)[0] }
    prob.`n-step semi-gradient TD`(
        v = func, π = π, n = 10,
        α = 2e-4,
        episodes = 100000)
    prob.apply {
      val line = Line("n-step semi-gradient TD")
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