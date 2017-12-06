package lab.mars.rl.algo.dp

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.IndexedMDP
import lab.mars.rl.model.impl.mdp.StateValueFunction
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.*
import org.apache.commons.math3.util.FastMath.*

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
fun IndexedMDP.ValueIteration(): StateValueFunction {
  val θ = 1e-6
  val V = VFunc { 0.0 }
  val PI = VFunc { null_action }
  //value iteration
  do {
    var Δ = 0.0
    for (s in states)
      if (s.isNotTerminal) {
        val v = V[s]
        V[s] = max(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
        Δ = max(Δ, abs(v - V[s]))
      }
    log.debug { "Δ=$Δ" }
  } while (Δ >= θ)
  //policy generation
  for (s in states)
    if (s.isNotTerminal)
      PI[s] = argmax(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
  return V
}