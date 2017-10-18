package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.argmax
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
class MonteCarlo(val mdp: MDP, private var policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var epsilon = 0.1

    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val preReturn = mdp.VFunc { Double.NaN }
        val count = mdp.VFunc { 0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            var accumulate = 0.0
            while (s.isNotTerminal()) {
                val a = s.actions.rand(policy(s))
                val possible = a.sample()
                if (preReturn[s].isNaN())
                    preReturn[s] = accumulate
                accumulate += possible.reward
                s = possible.next
            }
            preReturn.set { idx, value ->
                if (!value.isNaN()) {
                    V[idx] += accumulate - value
                    count[idx] += 1
                }
                Double.NaN
            }
        }
        for (s in states) {
            val n = count[s]
            if (n > 0)
                V[s] = V[s] / n
        }
        return V
    }

    fun `Optimal Exploring Starts`(): OptimalSolution {
        if (policy.isEmpty()) {
            policy = mdp.QFunc { 0.0 }
            for (s in started)
                policy[s, s.actions.first()] = 1.0
        }
        val Q = mdp.QFunc { 0.0 }
        val tmpQ = mdp.QFunc { Double.NaN }
        val count = mdp.QFunc { 0 }
        val tmpS = newBuf<State>(states.size)

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            var a = s.actions.rand()//Exploring Starts

            var accumulate = 0.0
            do {
                val possible = a.sample()
                if (tmpQ[s, a].isNaN())
                    tmpQ[s, a] = accumulate
                accumulate += possible.reward
                s = possible.next
            } while (s.isNotTerminal().apply { if (this) a = s.actions.rand(policy(s)) })

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
                val a_greedy = argmax(s.actions) {
                    val n = count[s, it]
                    if (n > 0)
                        Q[s, it] / n
                    else
                        Q[s, it]
                }
                for (a in s.actions)
                    policy[s, a] = if (a === a_greedy) 1.0 else 0.0
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
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }

    fun `On-policy first-visit MC control`(): OptimalSolution {
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
                val possible = a.sample()
                if (tmpQ[s, a].isNaN())
                    tmpQ[s, a] = accumulate
                accumulate += possible.reward
                s = possible.next
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
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }


    fun `Off-policy MC prediction`(): StateValueFunction {
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
                val possible = a.sample()
                S.append(possible.next)
                R.append(possible.reward)
                s = possible.next
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


    fun `Off-policy MC Optimal`(): OptimalSolution {
        val Q = mdp.QFunc { 0.0 }
        val C = mdp.QFunc { 0.0 }
        val b = mdp.QFunc { 1.0 }
        for (s in states) {
            if (s.isTerminal()) continue
            val prob = 1.0 / s.actions.size
            for (a in s.actions)
                b[s, a] = prob
        }
        val pi = mdp.QFunc { 1.0 }

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
                val possible = a.sample()
                S.append(possible.next)
                R.append(possible.reward)
                s = possible.next
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

                val `a*` = argmax(s_t.actions) { Q[s_t, it] }
                for (a in s_t.actions) {
                    pi[s_t, a] = when {
                        a === `a*` -> 1.0
                        else -> 0.0
                    }
                }
                if (a_t !== `a*`) break
                W = W * 1 / b[s_t, a_t]
            }
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(pi, V, Q)
        V_from_Q_ND(states, result)
        return result
    }
}