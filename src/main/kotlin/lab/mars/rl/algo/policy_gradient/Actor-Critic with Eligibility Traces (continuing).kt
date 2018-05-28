package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.MatrixSpec
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.matrix.Σ
import kotlin.math.exp

fun <E> MDP.`Actor-Critic with Eligibility Traces (continuing)`(
    h: LinearFunc<E>, α_θ: Double, λ_θ: Double,
    v: ApproximateFunction<E>, α_w: Double, λ_w: Double, η: Double,
    episodes: Int,
    z_maker: (Int, Int) -> MatrixSpec = { m, n -> Matrix(m, n) }) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    val z_θ = z_maker(h.w.size, 1)
    val z_w = z_maker(v.w.size, 1)
    var averageR = 0.0
    while (s.isNotTerminal) {
      step++
      val a = rand(s.actions) { exp(h(s, it)) }
      val (s_next, reward) = a.sample()
      val δ = reward - averageR + γ * if (s_next.isTerminal) 0.0 else v(s_next) - v(s)
      averageR += η * δ
      z_w `=` λ_w * z_w + v.`∇`(s)
      val `∇` = h.x(s, a) - Σ(s.actions) { b ->
        val tmp = h(s, b)
        h.x(s, b) / s.actions.sumByDouble { exp(h(s, it) - tmp) }
      }
      z_θ `=` λ_θ * z_θ + `∇`
      v.w += α_w * δ * z_w
      h.w += α_θ * δ * z_θ
      s = s_next
    }
  }
}