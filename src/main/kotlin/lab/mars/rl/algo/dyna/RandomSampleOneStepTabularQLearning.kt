package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.collection.filter
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple3

fun IndexedMDP.RandomSampleOneStepTabularQLearning(
    ε: Double,
    α: (IndexedState, IndexedAction) -> Double,
    episodes: Int): OptimalSolution {
  
  val Q = QFunc { 0.0 }
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    val s = started()
    val a = s.actions.rand()//Exploring Starts
    val (s_next, reward) = a.sample()
    Q[s, a] += α(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
  }
  val π = IndexedPolicy(QFunc { 0.0 })
  for (s in states.filter { it.isNotTerminal })
    `ε-greedy`(s, Q, π, ε)
  val V = VFunc { 0.0 }
  val result = tuple3(π, V, Q)
  V_from_Q(states, result)
  return result
}

