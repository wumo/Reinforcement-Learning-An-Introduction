package lab.mars.rl.algo.mc

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.mc.MonteCarlo.Companion.log
import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.tuples.tuple3

fun MonteCarlo.`Off-policy MC Optimal`(): OptimalSolution {
    val Q = indexedMdp.QFunc { 0.0 }
    val C = indexedMdp.QFunc { 0.0 }
    val b = indexedMdp.QFunc { 1.0 }
    for (s in states) {
        if (s.isTerminal()) continue
        val prob = 1.0 / s.actions.size
        for (a in s.actions)
            b[s, a] = prob
    }
    val π = indexedMdp.QFunc { 1.0 }

    val R = newBuf<Double>()
    val S = newBuf<IndexedState>()
    val A = newBuf<IndexedAction>()

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        S.clear(); S.append(s)
        R.clear();R.append(0.0)
        A.clear()
        var T = 0
        while (s.isNotTerminal()) {
            val a = s.actions.rand(b(s))
            A.append(a)
            val (s_next, reward) = a.sample()
            S.append(s_next)
            R.append(reward)
            s = s_next
            T++
        }
        var G = 0.0
        var W = 1.0
        for (t in T - 1 downTo 0) {
            val s_t = S[t]
            val a_t = A[t]
            G = γ * G + R[t + 1]
            C[s_t, a_t] += W
            Q[s_t, a_t] += W / C[s_t, a_t] * (G - Q[s_t, a_t])

            val `a*` = argmax(s_t.actions) { Q[s_t, it] }
            for (a in s_t.actions) {
                π[s_t, a] = when {
                    a === `a*` -> 1.0
                    else -> 0.0
                }
            }
            if (a_t !== `a*`) break
            W = W * 1 / b[s_t, a_t]
        }
    }
    val V = indexedMdp.VFunc { 0.0 }
    val result = tuple3(IndexedPolicy(π), V, Q)
    V_from_Q(states, result)
    return result
}