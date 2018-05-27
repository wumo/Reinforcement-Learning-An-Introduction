package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.*
import kotlin.math.exp

fun <E> MDP.`Actor-Critic with Eligibility Traces (episodic)`(
    h: LinearFunc<E>, α_θ: Double, λ_θ: Double,
    v: ApproximateFunction<E>, α_w: Double, λ_w: Double,
    episodes: Int,
    z_maker: (Int, Int) -> MatrixSpec = { m, n -> Matrix(m, n) },
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int, State, Double) -> Unit = { _, _, _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var G = 0.0
    var s = started()
    val z_θ = z_maker(h.w.size, 1)
    val z_w = z_maker(v.w.size, 1)
    var γ_t = 1.0
    while (s.isNotTerminal) {
      step++
      val a = rand(s.actions) { exp(h(s, it)) }
      val (s_next, reward) = a.sample()
      G += γ_t * reward
      val δ = reward + γ * (if (s_next.isTerminal) 0.0 else v(s_next)) - v(s)
      z_w *= γ * λ_w
      z_w += γ_t * v.`∇`(s)
//      z_w `=` γ * λ_w * z_w + γ_t * v.`∇`(s)
      val `∇` = h.x(s, a) - Σ(s.actions) { b ->
        val tmp = h(s, b)
        h.x(s, b) / s.actions.sumByDouble { exp(h(s, it) - tmp) }
      }
      z_θ *= γ * λ_θ
      z_θ += γ_t * `∇`
//      z_θ `=` γ * λ_θ * z_θ + γ_t * `∇`
      v.w += α_w * δ * z_w
      h.w += α_θ * δ * z_θ
      γ_t *= γ
      stepListener(episode, step, s, a)
      s = s_next
      if (step >= maxStep) break
    }
    episodeListener(episode, step, s, G)
  }
}