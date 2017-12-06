package lab.mars.rl.algo.eligibility_trace.control

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.matrix.*
import lab.mars.rl.util.matrix.Matrix.Companion.column
import lab.mars.rl.util.matrix.Matrix.Companion.one
import kotlin.Int.Companion

fun <E> MDP.`Sarsa(λ) accumulating trace`(
    Q: ApproximateFunction<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val w = Q.w
  val d = w.size
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    var z = column(d)
    while (true) {
      val (s_next, reward) = a.sample()
      z = γ * λ * z + Q.`▽`(s, a)
      var δ = reward - Q(s, a)
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        δ += γ * Q(s_next, a_next)
        w += α * δ * z
        s = s_next
        a = a_next
      } else {
        w += α * δ * z
        break
      }
      step++
      if (step >= maxStep)
        break
    }
    episodeListener(episode, step)
  }
}

inline fun <E> MDP.`Sarsa(λ) dutch trace`(
    Q: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    crossinline episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  `Sarsa(λ) linear trace`({ z, x, _, _ ->
                            z `=` (γ * λ * z + (1.0 - α * γ * λ * z.T * x) * x)
                          }, Q, π, α, episodes, maxStep, { e, s -> episodeListener(e, s) })
}

inline fun <E> MDP.`Sarsa(λ) replacing trace`(
    Q: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    crossinline episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val one = one(Q.w.size)
  `Sarsa(λ) linear trace`({ z, x, _, _ ->
                            z `=` (((one - x) o (γ * λ * z)) + x)
                          }, Q, π, α, episodes, maxStep, { e, s -> episodeListener(e, s) })
}

inline fun <E> MDP.`Sarsa(λ) replacing trace with clearing`(
    Q: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    crossinline episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val X = Q.x
  val one = one(Q.w.size)
  `Sarsa(λ) linear trace`({ z, x, s, a ->
                            z *= γ * λ
                            z `o=` (one - x)
                            for (_a in s.actions)
                              if (_a !== a)
                                z `o=` (one - X(s, _a))
                            z += x
                          }, Q, π, α, episodes, maxStep, { e, s -> episodeListener(e, s) })
}

fun <E> MDP.`Sarsa(λ) linear trace`(
    traceOp: (Matrix, Matrix, State, Action<*>) -> Unit,
    Q: LinearFunc<E>,
    π: Policy,
    α: Double,
    episodes: Int,
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int) -> Unit = { _, _ -> }) {
  val X = Q.x
  val w = Q.w
  val d = w.size
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    val s = started()
    var a = π(s)
    var x = X(s, a)
    val z = column(d)
    while (true) {
      val (s_next, reward) = a.sample()
      traceOp(z, x, s, a)
      var δ = reward - w.T * x
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        val `x'` = X(s_next, a_next)
        δ += γ * (w.T * `x'`)
        w += α * δ * z
        x = `x'`
        a = a_next
      } else {
        w += α * δ * z
        break
      }
      step++
      if (step >= maxStep) break
    }
    episodeListener(episode, step)
  }
}
