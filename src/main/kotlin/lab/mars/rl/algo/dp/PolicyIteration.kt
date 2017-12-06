package lab.mars.rl.algo.dp

import lab.mars.rl.algo.Q_from_V
import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.abs
import org.apache.commons.math3.util.FastMath.max
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
class PolicyIteration(indexedMdp: IndexedMDP) {
  companion object {
    val log = LoggerFactory.getLogger(this::class.java)!!
  }
  
  val θ = 1e-6
  val states = indexedMdp.states
  private val γ = indexedMdp.γ
  private val V = indexedMdp.VFunc { 0.0 }
  private val π = IndexedPolicy(indexedMdp.QFunc { 1.0 })
  private val Q = indexedMdp.QFunc { 0.0 }
  
  fun v_iteration(): OptimalSolution {
    do {
      //Policy Evaluation
      do {
        var Δ = 0.0
        for (s in states)
          if (s.isNotTerminal) {
            val v = V[s]
            V[s] = Σ(π(s).possibles) { probability * (reward + γ * V[next]) }
            Δ = max(Δ, abs(v - V[s]))
          }
        log.debug { "Δ=$Δ" }
      } while (Δ >= θ)
      
      //Policy Improvement
      var `policy-stable` = true
      for (s in states)
        if (s.isNotTerminal) {
          val `old-action` = π(s)
          val `new-action` = argmax(s.actions) { Σ(possibles) { probability * (reward + γ * V[next]) } }
          π[s] = `new-action`
          if (`old-action` !== `new-action`) `policy-stable` = false
        }
    } while (!`policy-stable`)
    val result = tuple3(π, V, Q)
    Q_from_V(γ, states, result)
    return result
  }
  
  fun q_iteration(): OptimalSolution {
    do {
      //Policy Evaluation
      do {
        var Δ = 0.0
        for ((s, a) in states { actions }) {
          val q = Q[s, a]
          Q[s, a] = Σ(a.possibles) { probability * (reward + γ * if (next.actions.any()) Q[next, π(next)] else 0.0) }
          Δ = max(Δ, abs(q - Q[s, a]))
        }
        log.debug { "Δ=$Δ" }
      } while (Δ >= θ)
      
      //Policy Improvement
      var `policy-stable` = true
      for (s in states)
        if (s.isNotTerminal) {
          val `old-action` = π(s)
          val `new-action` = argmax(s.actions) { Q[s, it] }
          π[s] = `new-action`
          if (`old-action` !== `new-action`) `policy-stable` = false
        }
    } while (!`policy-stable`)
    val result = tuple3(π, V, Q)
    V_from_Q(states, result)
    return result
  }
  
}