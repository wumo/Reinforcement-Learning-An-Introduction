package lab.mars.rl.algo.mc

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.mc.MonteCarlo.Companion.log
import lab.mars.rl.model.Action
import lab.mars.rl.model.State
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug

fun MonteCarlo.`Off-policy MC prediction`(): StateValueFunction {
    val Q = mdp.QFunc { 0.0 }
    val C = mdp.QFunc { 0.0 }
    val b = mdp.QFunc { 1.0 }
    for (s in states) {
        if (s.isTerminal()) continue
        val prob = 1.0 / s.actions.size
        for (a in s.actions)
            b[s, a] = prob
    }

    val R = newBuf<Double>()
    val S = newBuf<State>()
    val A = newBuf<Action>()

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
            val (s_next, reward, _) = a.sample()
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
            G = gamma * G + R[t + 1]
            C[s_t, a_t] += W
            Q[s_t, a_t] += W / C[s_t, a_t] * (G - Q[s_t, a_t])
            W = W * policy[s_t, a_t] / b[s_t, a_t]
            if (W == 0.0) break
        }
    }
    val V = mdp.VFunc { 0.0 }
    val result = Triple(policy, V, Q)
    V_from_Q_ND(states, result)
    return V
}