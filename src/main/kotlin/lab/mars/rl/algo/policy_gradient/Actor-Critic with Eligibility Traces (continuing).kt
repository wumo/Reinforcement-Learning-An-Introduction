package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.*

fun <E> FunctionApprox.`Actor-Critic with Eligibility Traces (continuing)`(
  π: ApproximateFunction<E>, α_θ: Double, λ_θ: Double,
  v: ApproximateFunction<E>, α_w: Double, λ_w: Double, η: Double) {
  for (episode in 1..episodes) {
    FunctionApprox.log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var z_θ = Matrix.column(π.w.size)
    var z_w = Matrix.column(v.w.size)
    var averageR = 0.0
    while (s.isNotTerminal) {
      step++
      val a = rand(s.actions) { π(s, it) }
      val (s_next, reward) = a.sample()
      val δ = reward - averageR + γ * if (s_next.isTerminal) 0.0 else v(s_next) - v(s)
      averageR += η * δ
      z_w = λ_w * z_w + v.`▽`(s)
      val `▽` = if (π is LinearFunc)
        π.x(s, a) - Σ(s.actions) { π(s, it) * π.x(s, it) }
      else
        π.`▽`(s, a) / π(s, a)
      z_θ = λ_θ * z_θ + `▽`
      v.w += α_w * δ * z_w
      π.w += α_θ * δ * z_θ
      s = s_next
    }
  }
}