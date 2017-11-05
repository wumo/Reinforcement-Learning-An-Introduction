package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.Possible
import lab.mars.rl.model.impl.CNSetMDP
import lab.mars.rl.util.cnsetOf
import lab.mars.rl.util.emptyNSet

/**
 * <p>
 * Created on 2017-10-10.
 * </p>
 *
 * @author wumo
 */
object RandomWalk {
    fun make(): Pair<MDP, NonDeterminedPolicy> {
        val mdp = CNSetMDP(1.0, 7, 1)
        mdp.apply {
            states[0].actions = emptyNSet()
            states[6].actions = emptyNSet()
            started = states(3)
            for (a in 1..5) {
                states[a].actions.apply {
                    this[0].possibles = cnsetOf(Possible(states[a - 1], 0.0, 0.5),
                                                Possible(states[a + 1], if (a == 5) 1.0 else 0.0, 0.5))
                }
            }
        }
        val policy = mdp.QFunc { 1.0 }
        return Pair(mdp, policy)
    }
}