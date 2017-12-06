package lab.mars.rl.algo.td

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.log.debug

fun IndexedMDP.`Tabular TD(0)`(π: IndexedPolicy, episodes: Int, α: Double): StateValueFunction {
  val V = VFunc { 0.0 }
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    while (s.isNotTerminal) {
      val a = π(s)
      val (s_next, reward) = a.sample()
      V[s] += α * (reward + γ * V[s_next] - V[s])
      s = s_next
    }
  }
  return V
}