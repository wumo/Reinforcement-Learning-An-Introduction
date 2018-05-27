package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.matrix.Σ
import kotlin.math.exp

fun <E> MDP.`One-step Actor-Critic (episodic)`(
    h: LinearFunc<E>, α_θ: Double,
    v: ApproximateFunction<E>, α_w: Double,
    episodes: Int) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var γ_t = 1.0
    while (s.isNotTerminal) {
      step++
      val a = rand(s.actions) { exp(h(s, it)) }
      val (s_next, reward) = a.sample()
      val δ = reward + γ * (if (s_next.isTerminal) 0.0 else v(s_next)) - v(s)
      v.w += α_w * γ_t * δ * v.`∇`(s)
      val `∇` = h.x(s, a) - Σ(s.actions) { b ->
        val tmp = h(s, b)
        h.x(s, b) / s.actions.sumByDouble { exp(h(s, it) - tmp) }
      }
      h.w += α_θ * γ_t * δ * `∇`
      γ_t *= γ
      s = s_next
    }
  }
}