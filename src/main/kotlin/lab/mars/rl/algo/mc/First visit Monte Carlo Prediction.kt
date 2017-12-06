package lab.mars.rl.algo.mc

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.log.debug

fun IndexedMDP.`First Visit Monte Carlo Prediction`(π: IndexedPolicy, episodes: Int): StateValueFunction {
  val V = VFunc { 0.0 }
  val preReturn = VFunc { Double.NaN }
  val count = VFunc { 0 }
  
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    var accumulate = 0.0
    while (s.isNotTerminal) {
      val a = π(s)
      val (s_next, reward) = a.sample()
      if (preReturn[s].isNaN())
        preReturn[s] = accumulate
      accumulate += reward
      s = s_next
    }
    preReturn.set { idx, value ->
      if (!value.isNaN()) {
        V[idx] += accumulate - value
        count[idx] += 1
      }
      Double.NaN
    }
  }
  for (s in states) {
    val n = count[s]
    if (n > 0)
      V[s] = V[s] / n
  }
  return V
}