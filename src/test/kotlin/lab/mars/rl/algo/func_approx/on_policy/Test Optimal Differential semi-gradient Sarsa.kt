package lab.mars.rl.algo.func_approx.on_policy

import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.model.impl.func.SimpleTileCoding
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.problem.AccessControl
import lab.mars.rl.util.color
import lab.mars.rl.util.reset
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
    val π = `ε-greedy function policy`(func, 0.1)
    prob.`Differential semi-gradient Sarsa`(
        q = func,
        π = π,
        α = 0.01,
        β = 0.01,
        maxStep = 1000_0000)
    for (pr in AccessControl.priorities) {
      for (fs in 0..AccessControl.k) {
        val s = prob.states[fs, pr]
        print("${color(1 - (π.greedy(s) as IndexedAction)[0])} ${reset()}")
      }
      println()
    }
  }
}