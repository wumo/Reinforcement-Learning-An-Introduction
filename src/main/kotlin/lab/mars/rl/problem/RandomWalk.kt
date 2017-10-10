package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.Possible
import lab.mars.rl.model.impl.CNSetMDP
import lab.mars.rl.util.emptyNSet
import java.util.concurrent.ThreadLocalRandom

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
            for (a in 1..5) {
                states[a].actions.apply {
                    this[0].sample = {
                        if (ThreadLocalRandom.current().nextBoolean())
                            Possible(states[a - 1], 0.0, 1.0)
                        else
                            Possible(states[a + 1], if (a == 5) 1.0 else 0.0, 1.0)
                    }
                }
            }
        }
        val policy = mdp.QFunc { 1.0 }
        return Pair(mdp, policy)
    }
}