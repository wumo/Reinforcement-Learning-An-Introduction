package lab.mars.rl.algo.func_approx.on_policy

import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.matrix.times

fun <E> MDP.`Episodic semi-gradient Sarsa control`(
    Qfunc: ApproximateFunction<E>,
    π: Policy,
    α: Double,
    episodes: Int,
    episodeListener: (Int, Int, State, Double) -> Unit = { _, _, _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    var G = 0.0
    var γn = 1.0
    while (true) {
      step++
      stepListener(episode, step, s, a)
      val (s_next, reward) = a.sample()
      γn *= γ
      G += γn * reward
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        Qfunc.w += α * (reward + γ * Qfunc(s_next, a_next) - Qfunc(s, a)) * Qfunc.`∇`(s, a)
        s = s_next
        a = a_next
      } else {
        Qfunc.w += α * (reward - Qfunc(s, a)) * Qfunc.`∇`(s, a)
        s = s_next
        break
      }
    }
    episodeListener(episode, step, s, G)
  }
}