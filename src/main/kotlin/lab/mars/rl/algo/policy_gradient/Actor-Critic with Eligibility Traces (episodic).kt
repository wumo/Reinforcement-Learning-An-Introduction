package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.*

fun <E> MDP.`Actor-Critic with Eligibility Traces (episodic)`(
    π: ApproximateFunction<E>, α_θ: Double, λ_θ: Double,
    v: ApproximateFunction<E>, α_w: Double, λ_w: Double,
    episodes: Int) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var z_θ = Matrix.column(π.w.size)
    var z_w = Matrix.column(v.w.size)
    var γ_t = 1.0
    while (s.isNotTerminal) {
      step++
      val a = rand(s.actions) { π(s, it) }
      val (s_next, reward) = a.sample()
      val δ = reward + γ * if (s_next.isTerminal) 0.0 else v(s_next) - v(s)
      z_w = γ * λ_w * z_w + γ_t * v.`▽`(s)
      val `▽` = if (π is LinearFunc)
        π.x(s, a) - Σ(s.actions) { π(s, it) * π.x(s, it) }
      else
        π.`▽`(s, a) / π(s, a)
      z_θ = γ * λ_θ * z_θ + γ_t * `▽`
      v.w += α_w * γ_t * δ * z_w
      π.w += α_θ * γ_t * δ * z_θ
      γ_t *= γ
      s = s_next
    }
  }
}