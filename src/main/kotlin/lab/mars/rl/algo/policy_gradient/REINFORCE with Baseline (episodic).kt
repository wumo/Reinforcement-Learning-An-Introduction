package lab.mars.rl.algo.policy_gradient

import lab.mars.rl.algo.func_approx.FunctionApprox
import lab.mars.rl.algo.func_approx.FunctionApprox.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.rand
import lab.mars.rl.util.matrix.times
import lab.mars.rl.util.matrix.Σ

fun <E> FunctionApprox.`REINFORCE with Baseline (episodic)`(
  π: ApproximateFunction<E>, α_θ: Double,
  v: ApproximateFunction<E>, α_w: Double) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    val s = started()
    var a = rand(s.actions) { π(s, it) }
    val S = newBuf<State>()
    val A = newBuf<Action<State>>()
    val R = newBuf<Double>()

    S.append(s)
    R.append(0.0)
    var accu = 0.0
    var T: Int
    while (true) {
      step++
      A.append(a)
      val (s_next, reward) = a.sample()
      accu += reward
      R.append(accu)
      S.append(s_next)
      if (s_next.isTerminal()) {
        T = step
        break
      }
      a = rand(s.actions) { π(s, it) }
    }
    var γ_t = 1.0
    for (t in 0 until T) {
      val G = accu - R[t]
      val δ = G - v(S[t])
      v.w += α_w * γ_t * δ * v.`▽`(S[t])
      val `▽` = if (π is LinearFunc)
        π.x(S[t], A[t]) - Σ(S[t].actions) { π(S[t], it) * π.x(S[t], it) }
      else
        π.`▽`(S[t], A[t]) / π(S[t], A[t])
      π.w += α_θ * γ_t * δ * `▽`
      γ_t *= γ
    }
  }
}