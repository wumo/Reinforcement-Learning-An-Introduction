package lab.mars.rl.algo.dp

import lab.mars.rl.algo.Q_from_V
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.collection.filter
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.*
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.*

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
fun IndexedMDP.ValueIteration(): OptimalSolution {
  val V = VFunc { 0.0 }
  val π = IndexedPolicy(QFunc { 1.0 })
  val Q = QFunc { 0.0 }
  //value iteration
  do {
    var Δ = 0.0
    for (s in states.filter { it.isNotTerminal }) {
      val v = V[s]
      V[s] = max(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
      Δ = max(Δ, abs(v - V[s]))
    }
    log.debug { "Δ=$Δ" }
  } while (Δ >= θ)
  //policy generation
  for (s in states.filter { it.isNotTerminal })
    π[s] = argmax(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
  val result = tuple3(π, V, Q)
  Q_from_V(γ, states, result)
  return result
}