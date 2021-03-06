@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.mc

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.model.impl.mdp.IndexedMDP
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.model.impl.mdp.OptimalSolution
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.collection.fork
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.tuples.tuple3

fun IndexedMDP.`On-policy first-visit MC control`(episodes: Int): OptimalSolution {
  val ε = 0.1
  val π = equiprobablePolicy()
  val Q = QFunc { 0.0 }
  val tmpQ = QFunc { Double.NaN }
  val count = QFunc { 0 }
  val tmpS = newBuf<IndexedState>(states.size)
  
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    var accumulate = 0.0
    while (s.isNotTerminal) {
      val a = π(s)
      val (s_next, reward) = a.sample()
      if (tmpQ[s, a].isNaN())
        tmpQ[s, a] = accumulate
      accumulate += reward
      s = s_next
    }
    tmpS.clear()
    for ((s, a) in states.fork { it.actions }) {
      val value = tmpQ[s, a]
      if (!value.isNaN()) {
        Q[s, a] += accumulate - value
        count[s, a] += 1
        tmpS.append(s)
        tmpQ[s, a] = Double.NaN
      }
    }
    for (s in tmpS) {
      val a_opt = argmax(s.actions) {
        val n = count[s, it]
        if (n > 0)
          Q[s, it] / n
        else
          Q[s, it]
      }
      val size = s.actions.size
      for (a in s.actions) {
        π[s, a] = when {
          a === a_opt -> 1 - ε + ε / size
          else -> ε / size
        }
      }
    }
  }
  
  Q.set { idx, value ->
    val n = count[idx]
    if (n > 0)
      value / n
    else
      value
  }
  val V = VFunc { 0.0 }
  val result = tuple3(π, V, Q)
  V_from_Q(states, result)
  return result
}