package lab.mars.rl.algo.func_approx.on_policy

import lab.mars.rl.model.*
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.matrix.times

fun <E> MDP.`Episodic semi-gradient QLearning control`(
    Q: ApproximateFunction<E>,
    π: Policy,
    α: Double,
    episodes: Int,
    episodeListener: (Int, Int) -> Unit = { _, _ -> },
    stepListener: (Int, Int, State, Action<State>) -> Unit = { _, _, _, _ -> }) {
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var step = 0
    var s = started()
    var a = π(s)
    while (true) {
      step++
      stepListener(episode, step, s, a)
      val (s_next, reward) = a.sample()
      if (s_next.isNotTerminal) {
        val a_next = π(s_next)
        Q.w += α * (reward + γ * max(s_next.actions) { Q(s_next, it) } - Q(s, a)) * Q.`∇`(s, a)
        s = s_next
        a = a_next
      } else {
        Q.w += α * (reward - Q(s, a)) * Q.`∇`(s, a)
        break
      }
    }
    episodeListener(episode, step)
  }
}