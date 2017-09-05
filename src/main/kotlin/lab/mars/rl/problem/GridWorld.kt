package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.impl.DimNSetMDP

/**
 * <p>
 * Created on 2017-09-05.
 * </p>
 *
 * @author wumo
 */
object GridWorld {
    const val n = 4
    const val action_num = 4

    fun make(): MDP {
        val mdp = DimNSetMDP(state_dim = intArrayOf(n, n),
                action_dim = intArrayOf(action_num))
        mdp.states.forEach {  }

        return mdp
    }
}