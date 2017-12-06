package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.tuples.tuple3

fun IndexedMDP.expectedSarsa(
    ε: Double,
    α: (IndexedState, IndexedAction) -> Double,
    episodes: Int): OptimalSolution {
  val π = IndexedPolicy(QFunc { 0.0 })
  val Q = QFunc { 0.0 }
  
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    while (s.isNotTerminal) {
      `ε-greedy`(s, Q, π, ε)
      val a = π(s)
      val (s_next, reward) = a.sample()
      Q[s, a] += α(s, a) * (reward + γ * Σ(s_next.actions) { π[s_next, it] * Q[s_next, it] } - Q[s, a])
      s = s_next
    }
  }
  val V = VFunc { 0.0 }
  val result = tuple3(π, V, Q)
  V_from_Q(states, result)
  return result
}