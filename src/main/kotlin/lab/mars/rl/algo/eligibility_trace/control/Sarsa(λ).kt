package lab.mars.rl.algo.eligibility_trace.control

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.func.Feature
import lab.mars.rl.model.impl.func.LinearFunc
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.*
import lab.mars.rl.util.matrix.Matrix.Companion.column
import lab.mars.rl.util.matrix.Matrix.Companion.one

fun <E> MDP.`Sarsa(λ) linear trace`(
    traceOp: TraceType,
    Q: LinearFunc<E>,
    π: Policy,
    λ: Double,
    α: Double,
    episodes: Int,
    z_maker: (Int, Int) -> MatrixSpec = { m, n -> Matrix(m, n) },
    maxStep: Int = Int.MAX_VALUE,
    episodeListener: (Int, Int) -> Unit = { _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  val X = Q.x
  val w = Q.w
  val d = w.size
  val z = z_maker(d, 1)
  val ctx = Context(γ, α, λ, z, X, one(d))
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    var x = X(s, a)
    z.zero()
    while (true) {
      step++
      stepListener(episode, step, s, a)
      if (step >= maxStep) break
      
      ctx.x = x; ctx.s = s; ctx.a = a
      traceOp(ctx)
      val (s_next, reward) = a.sample()
      var δ = reward - (w `T*` x)
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        val `x'` = X(s_next, a_next)
        δ += γ * (w `T*` `x'`)
        w += α * δ * z
        x = `x'`
        s = s_next
        a = a_next
      } else {
        w += α * δ * z
        break
      }
      
    }
    episodeListener(episode, step)
  }
}

val `accumulating trace`: TraceType = {
  it.apply {
    z `=` γ * λ * z + x
  }
}

val `dutch trace`: TraceType = {
  it.apply {
    z `=` (γ * λ * z + (1.0 - α * γ * λ * (z `T*` x)) * x)
  }
}

val `replacing trace`: TraceType = {
  it.apply {
    z `=` (((one - x) o (γ * λ * z)) + x)
  }
}

val `replacing trace with clearing`: TraceType = {
  it.apply {
    z *= γ * λ
    z `o=` (one - x)
    for (_a in s.actions)
      if (_a !== a)
        z `o=` (one - X(s, _a))
    z += x
  }
}

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
    val z = column(d)
    while (true) {
      val (s_next, reward) = a.sample()
      z `=` γ * λ * z + Q.`∇`(s, a)
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

typealias TraceType = (Context) -> Unit

class Context(val γ: Double,
              val α: Double,
              val λ: Double,
              val z: MatrixSpec,
              val X: Feature<*>,
              val one: MatrixSpec) {
  lateinit var x: MatrixSpec
  lateinit var s: State
  lateinit var a: Action<*>
}