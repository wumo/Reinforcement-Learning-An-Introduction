package lab.mars.rl.algo

import lab.mars.rl.model.*

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
class MonteCarlo(val mdp: MDP) {
    val states = mdp.states
    var max_iteration: Int = 10000
    fun prediction(policy: DeterminedPolicy): StateValueFunction {
        val V = mdp.stateFunc<Double> { 0.0 }
        val tmpV = mdp.stateFunc<Double> { Double.NaN }
        val count = mdp.stateFunc<Int> { 0 }
        for (_s in states) _s.actions.ifAny {
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
        for (s in states) {
            val n = count[s]
            if (n > 0)
                V[s] = V[s] / n
        }
        return V
    }

    fun iteration_ES(): Triple<DeterminedPolicy, StateValueFunction, ActionValueFunction> {
        val policy = mdp.stateFunc<Action> { states[it].actions.firstOrNull() ?: null_action }
        val Q = mdp.stateActionFunc<Double> { 0.0 }
        val tmpQ = mdp.stateActionFunc<Double> { Double.NaN }
        val count = mdp.stateActionFunc<Int> { 0 }
        for (_s in states)
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
                    tmpQ.set { idx, value ->
                        if (!value.isNaN()) {
                            Q[idx] += value
                            count[idx] += 1
                        }
                        Double.NaN
                    }
                    policy.set { idx, _ ->
                        val s = states[idx]
                        if (s.actions.isEmpty()) null_action
                        else argmax(s.actions) {
                            val n = count[idx]
                            if (n > 0)
                                Q[idx] / n
                            else
                                Q[idx]
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
        val V = mdp.stateActionFunc<Double> { 0.0 }
        V_from_Q(mdp.gamma, states, V, Q)
        return Triple(policy, V, Q)
    }

    fun iteration() {

    }
}