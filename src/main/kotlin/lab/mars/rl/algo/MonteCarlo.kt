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
    val gamma = mdp.gamma
    val V = mdp.v_maker()
    val PI = mdp.pi_maker()
    val Q = mdp.q_maker()

    fun prediction(policy: DeterminedPolicy, iteration: Int): StateValueFunction {
        val V=mdp.v_maker()
        val tmpV = mdp.v_maker()
        tmpV.set { _, _ -> Double.NaN }
        val count = mdp.v_maker()
        for (_s in states) _s.actions.ifAny {
            for (i in 0 until iteration) {
                var s = _s
                var accumulate = 0.0
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
                        count[idx] += 1.0
                    }
                    Double.NaN
                }
            }
        }
        for (s in states) {
            val n = count[s].toInt()
            if (n > 0)
                V[s] = V[s] / n
        }
        return V
    }

    fun iteration() {

    }
}