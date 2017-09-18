package lab.mars.rl.algo

import lab.mars.rl.model.*
import lab.mars.rl.util.ifAny

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
class MonteCarlo(mdp: MDP) {
    val states = mdp.states
    val gamma = mdp.gamma
    val V = mdp.v_maker()
    val PI = mdp.pi_maker()
    val Q = mdp.q_maker()

    fun prediction(policy: DeterminedPolicy, iteration: Int): StateValueFunction {
        for (_s in states) {
            var a: Action? = policy[_s] ?: continue
            for (i in 0 until iteration) {
                var s = _s
                while (a != null) {
                    val possible = s.actions[a].sample()
                    evaluate(possible)
                    s = possible.next
                    a = policy[s]
                }
            }
        }
        return V
    }

    private fun evaluate(possible: Possible) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun iteration() {

    }
}