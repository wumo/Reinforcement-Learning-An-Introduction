package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.emptyNSet
import java.util.*

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
class MonteCarlo(val mdp: MDP, private var policy: DeterminedPolicy = emptyNSet()) {
    val states = mdp.states
    var max_iteration: Int = 10000
    private val rand = Random(System.nanoTime())

    fun predictionRand(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val tmpV = mdp.VFunc { Double.NaN }
        val count = mdp.VFunc { 0 }
        for (i in 0 until max_iteration) {
            val _s = states.at(rand.nextInt(states.size))
            println("$i/$max_iteration")
            _s.actions.ifAny {
                var accumulate = 0.0
                var s = _s
                var a = policy[_s]
                while (a !== null_action) {
                    val possible = s.actions[a].sample()
                    accumulate += possible.reward
                    if (tmpV[s].isNaN())
                        tmpV[s] = accumulate
                    s = possible.next
                    a = policy[s]
                }
                tmpV.set { idx, value ->
                    if (!value.isNaN()) {
                        V[idx] += value
                        count[idx] += 1
                    }
                    Double.NaN
                }
            }
        }
        for (s in states) {
            val n = count[s]
            if (n > 0)
                V[s] = V[s] / n
        }
        return V
    }

    fun prediction(): StateValueFunction {
        val V = mdp.VFunc { 0.0 }
        val tmpV = mdp.VFunc { Double.NaN }
        val count = mdp.VFunc { 0 }
        val total_states = states.size
        var iteration = 1
        for (_s in states) {
            println("${iteration++}/$total_states")
            _s.actions.ifAny {
                for (i in 0 until max_iteration) {
                    var accumulate = 0.0
                    var s = _s
                    var a = policy[_s]
                    while (a !== null_action) {
                        val possible = s.actions[a].sample()
                        accumulate += possible.reward
                        if (tmpV[s].isNaN())
                            tmpV[s] = accumulate
                        s = possible.next
                        a = policy[s]
                    }
                    tmpV.set { idx, value ->
                        if (!value.isNaN()) {
                            V[idx] += value
                            count[idx] += 1
                        }
                        Double.NaN
                    }
                }
            }
        }
        for (s in states) {
            val n = count[s]
            if (n > 0)
                V[s] = V[s] / n
        }
        return V
    }

    fun `Optimal Exploring Starts`(): Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        if (policy === emptyNSet)
            policy = mdp.VFunc { states[it].actions.firstOrNull() ?: null_action }
        val Q = mdp.QFunc { 0.0 }
        val tmpQ = mdp.QFunc { Double.NaN }
        val count = mdp.QFunc { 0 }
        val total_states = states.size
        var i = 1
        val tmpS = DefaultBuf.new<State>(states.size)
        for (_s in states) {
            println("${i++}/$total_states")
            for (_a in _s.actions) {
                for (i in 0 until max_iteration) {
                    var accumulate = 0.0
                    var s = _s
                    var a = _a
                    while (a !== null_action) {
                        val possible = s.actions[a].sample()
                        accumulate += possible.reward
                        if (tmpQ[s, a].isNaN())
                            tmpQ[s, a] = accumulate
                        s = possible.next
                        a = policy[s]
                    }
                    tmpS.clear()
                    for (s in states) {
                        if (s.actions.isEmpty()) continue
                        for (a in s.actions) {
                            val value = tmpQ[s, a]
                            if (!value.isNaN()) {
                                Q[s, a] += value
                                count[s, a] += 1
                                tmpS += s
                                tmpQ[s,a]=Double.NaN
                            }
                        }
                    }
                    for (s in tmpS) {
                        policy[s] = argmax(s.actions) {
                            val n = count[s, this]
                            if (n > 0)
                                Q[s, this] / n
                            else
                                Q[s, this]
                        }
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
        V_from_Q(states, result)
        return result
    }

    fun `Optimal Exploring Starts Random`(): Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        if (policy === emptyNSet)
            policy = mdp.VFunc { states[it].actions.firstOrNull() ?: null_action }
        val Q = mdp.QFunc { 0.0 }
        val tmpQ = mdp.QFunc { Double.NaN }
        val count = mdp.QFunc { 0 }
        val tmpS = DefaultBuf.new<State>(states.size)
        for (i in 0 until max_iteration) {
            println("$i/$max_iteration")
            val _s = states.at(rand.nextInt(states.size))
            if (_s.actions.isEmpty()) continue
            val _a = _s.actions.at(rand.nextInt(_s.actions.size))

            var accumulate = 0.0
            var s = _s
            var a = _a
            while (a !== null_action) {
                val possible = s.actions[a].sample()
                accumulate += possible.reward
                if (tmpQ[s, a].isNaN())
                    tmpQ[s, a] = accumulate
                s = possible.next
                a = policy[s]
            }
            tmpS.clear()
            for (s in states) {
                if (s.actions.isEmpty()) continue
                for (a in s.actions) {
                    val value = tmpQ[s, a]
                    if (!value.isNaN()) {
                        Q[s, a] += value
                        count[s, a] += 1
                        tmpS += s
                        tmpQ[s,a]=Double.NaN
                    }
                }
            }
            for (s in tmpS) {
                policy[s] = argmax(s.actions) {
                    val n = count[s, this]
                    if (n > 0)
                        Q[s, this] / n
                    else
                        Q[s, this]
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
        V_from_Q(states, result)
        return result
    }

    fun `On-policy first-visit MC control`(epsilon: Double = 0.1): Triple<NonDeterminedPolicy, StateValueFunction, ActionValueFunction> {
        val policy = mdp.QFunc { 0.0 }
        for (s in states) {
            if (s.actions.isEmpty()) continue
            val prob = 1.0 / s.actions.size
            for (a in s.actions)
                policy[s, a] = prob
        }
        val Q = mdp.QFunc { 0.0 }
        val tmpQ = mdp.QFunc { Double.NaN }
        val count = mdp.QFunc { 0 }
        val tmpS = DefaultBuf.new<State>(states.size)
        for (i in 0 until max_iteration) {
            println("$i/$max_iteration")
            var s = states.at(rand.nextInt(states.size))
            if (s.actions.isEmpty()) continue
            var accumulate = 0.0
            while (!s.actions.isEmpty()) {
                val a = s.actions.rand(policy(s))
                val possible = s.actions[a].sample()
                accumulate += possible.reward
                if (tmpQ[s, a].isNaN())
                    tmpQ[s, a] = accumulate
                s = possible.next
            }
            tmpS.clear()
            for (s in states) {
                if (s.actions.isEmpty()) continue
                for (a in s.actions) {
                    val value = tmpQ[s, a]
                    if (!value.isNaN()) {
                        Q[s, a] += value
                        count[s, a] += 1
                        tmpS += s
                        tmpQ[s,a]=Double.NaN
                    }
                }
            }
            for (s in tmpS) {
                val `a*` = argmax(s.actions) {
                    val n = count[s, this]
                    if (n > 0)
                        Q[s, this] / n
                    else
                        Q[s, this]
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

}