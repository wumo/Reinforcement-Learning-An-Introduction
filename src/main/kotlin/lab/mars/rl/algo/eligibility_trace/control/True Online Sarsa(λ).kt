package lab.mars.rl.algo.eligibility_trace.control

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*

fun <E> MDP.`True Online Sarsa(λ)`(
    Qfunc: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    z_maker: (Int, Int) -> MatrixSpec = { m, n -> Matrix(m, n) },
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int, State, Double) -> Unit = { _, _, _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  val X = Qfunc.x
  val w = Qfunc.w
  val d = w.size
  val z = z_maker(d, 1)
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    var x = X(s, a)
    z.zero()
    var Q_old = 0.0
    var G = 0.0
    var γn = 1.0
    while (true) {
      z `=` (γ * λ * z + (1.0 - α * γ * λ * (z `T*` x)) * x)
      val (s_next, reward) = a.sample()
      γn *= γ
      G += γn * reward
      s = s_next
      val Q = (w `T*` x).toScalar
      var δ = reward - Q
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        val `x'` = X(s_next, a_next)
        val `Q'` = (w `T*` `x'`).toScalar
        δ += γ * `Q'`
        w += α * (δ + Q - Q_old) * z - α * (Q - Q_old) * x
        Q_old = `Q'`
        x = `x'`
        a = a_next
      } else {
        w += α * (δ + Q - Q_old) * z - α * (Q - Q_old) * x
        break
      }
      step++
      stepListener(episode, step, s_next, a)
      if (step >= maxStep) break
    }
    episodeListener(episode, step, s, G)
  }
}