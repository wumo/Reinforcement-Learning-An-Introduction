package lab.mars.rl.algo.func_approx.on_policy

import javafx.application.Application
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SimpleTileCoding
import lab.mars.rl.model.impl.func.SuttonTileCoding
import lab.mars.rl.model.impl.mdp.EpsilonGreedyFunctionPolicy
import lab.mars.rl.model.impl.mdp.IndexedAction
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.problem.AccessControl
import lab.mars.rl.util.color
import lab.mars.rl.util.reset
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.ui.ChartApp
import lab.mars.rl.util.ui.D2DChart
import lab.mars.rl.util.ui.Line
import lab.mars.rl.util.ui.LineChart
import org.junit.Test

class `Test Optimal Differential semi-gradient Sarsa` {
  @Test
  fun `Access-Control Queuing Task`() {
    val prob = AccessControl.make()
    val func = LinearFunc(
        SimpleTileCoding(1,
                         prob.states.size * 2,
                         1,
                         0.0) { (s, a) ->
          val (fs, pr) = (s as IndexedState)
          val (_a) = (a as IndexedAction)
          (fs * 4 * 2 + pr * 2 + _a).toDouble()
        })
    val π = EpsilonGreedyFunctionPolicy(func, 0.1)
    prob.`Differential semi-gradient Sarsa`(
        q = func,
        π = π,
        α = 0.01,
        β = 0.01,
        maxStep = 100_0000)
    for (pr in AccessControl.priorities) {
      for (fs in 0..AccessControl.k) {
        val s = prob.states[fs, pr]
        print("${color(1 - (π.greedy(s) as IndexedAction)[0])} ${reset()}")
      }
      println()
    }
    val chart = LineChart("Differential",
                          "Number of free servers",
                          "Differential value of best action")
    for (pr in AccessControl.priorities) {
      val line = Line("priority $pr")
      for (fs in 0..AccessControl.k) {
        val s = prob.states[fs, pr]
        val a = π.greedy(s) as IndexedAction
        line[fs] = func(s, a)
      }
      chart += line
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
  
  @Test
  fun `Access-Control Queuing Task Sutton Tile Coding`() {
    val prob = AccessControl.make()
    val numTilings = 8
    val serverScale = numTilings / AccessControl.k.toDouble()
    val priorityScale = numTilings / 3.0
    val func = LinearFunc(
        SuttonTileCoding(255,
                         numTilings) { (s, a) ->
          val (fs, pr) = (s as IndexedState)
          val (_a) = (a as IndexedAction)
          tuple2(doubleArrayOf(fs * serverScale, pr * priorityScale),
                 intArrayOf(_a))
        })
    val π = EpsilonGreedyFunctionPolicy(func, 0.1)
    prob.`Differential semi-gradient Sarsa`(
        q = func,
        π = π,
        α = 0.01 / numTilings,
        β = 0.01,
        maxStep = 100_0000)
    for (pr in AccessControl.priorities) {
      for (fs in 0..AccessControl.k) {
        val s = prob.states[fs, pr]
        print("${color(1 - (π.greedy(s) as IndexedAction)[0])} ${reset()}")
      }
      println()
    }
    val chart = LineChart("Differential",
                          "Number of free servers",
                          "Differential value of best action")
    for (pr in AccessControl.priorities) {
      val line = Line("priority $pr")
      for (fs in 0..AccessControl.k) {
        val s = prob.states[fs, pr]
        val a = π.greedy(s) as IndexedAction
        line[fs] = func(s, a)
      }
      chart += line
    }
    D2DChart.charts += chart
    Application.launch(ChartApp::class.java)
  }
}