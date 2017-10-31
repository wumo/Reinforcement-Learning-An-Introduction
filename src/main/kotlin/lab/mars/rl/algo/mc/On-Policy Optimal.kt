@file:Suppress("NAME_SHADOWING")

package lab.mars.rl.algo.mc

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.mc.MonteCarlo.Companion.log
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.argmax
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import lab.mars.rl.util.tuples.tuple3

fun MonteCarlo.`On-policy first-visit MC control`(): OptimalSolution {
    val policy = mdp.equiprobablePolicy()
    val Q = mdp.QFunc { 0.0 }
    val tmpQ = mdp.QFunc { Double.NaN }
    val count = mdp.QFunc { 0 }
    val tmpS = newBuf<State>(states.size)

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        var accumulate = 0.0
        while (s.isNotTerminal()) {
            val a = s.actions.rand(policy(s))
            val (s_next, reward, _) = a.sample()
            if (tmpQ[s, a].isNaN())
                tmpQ[s, a] = accumulate
            accumulate += reward
            s = s_next
        }
        tmpS.clear()
        for (s in states) {
            if (s.isTerminal()) continue
            for (a in s.actions) {
                val value = tmpQ[s, a]
                if (!value.isNaN()) {
                    Q[s, a] += accumulate - value
                    count[s, a] += 1
                    tmpS.append(s)
                    tmpQ[s, a] = Double.NaN
                }
            }
        }
        for (s in tmpS) {
            val `a*` = argmax(s.actions) {
                val n = count[s, it]
                if (n > 0)
                    Q[s, it] / n
                else
                    Q[s, it]
            }
            val size = s.actions.size
            for (a in s.actions) {
                policy[s, a] = when {
                    a === `a*` -> 1 - epsilon + epsilon / size
                    else -> epsilon / size
                }
            }
        }
    }

    Q.set { idx, value ->
        val n = count[idx]
        if (n > 0)
            value / n
        else
            value
    }
    val V = mdp.VFunc { 0.0 }
    val result = tuple3(policy, V, Q)
    V_from_Q_ND(states, result)
    return result
}