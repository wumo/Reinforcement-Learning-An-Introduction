@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.func_approx.on_policy

import lab.mars.rl.algo.ntd.MAX_N
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.math.Σ
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.*

fun <E> MDP.`Differential semi-gradient n-step Sarsa`(
    q: ApproximateFunction<E>, π: Policy,
    n: Int,
    α: Double, β: Double) {
  var average_reward = 0.0
  val _R = newBuf<Double>(min(n, MAX_N))
  val _S = newBuf<State>(min(n, MAX_N))
  val _A = newBuf<Action<State>>(min(n, MAX_N))
  
  var t = 0
  val s = started()
  var a = π(s)
  _R.clear();_R.append(0.0)
  _S.clear();_S.append(s)
  _A.clear();_A.append(a)
  while (true) {
    if (t >= n) {//最多存储n个
      _R.removeFirst()
      _S.removeFirst()
      _A.removeFirst()
    }
    val (s_next, reward) = a.sample()
    _R.append(reward)
    _S.append(s_next)
    a = π(s)
    _A.append(a)
    val τ = t - n + 1
    if (τ >= 0) {
      val δ = Σ(1..n) { _R[it] - average_reward } + q(_S[n], _A[n]) - q(_S[0], _A[0])
      average_reward += β * δ
      q.w += α * δ * q.`▽`(_S[0], _A[0])
    }
    t++
  }
}