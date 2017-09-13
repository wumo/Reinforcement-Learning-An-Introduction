package lab.mars.rl.problem

import lab.mars.rl.model.MDP
import lab.mars.rl.model.impl.Dim
import lab.mars.rl.model.impl.NSetMDP
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.min

/**
 * <p>
 * Created on 2017-09-13.
 * </p>
 *
 * @author wumo
 */
class Gambler {
    val goal_coin = 100

    fun make(): MDP {
        val mdp = NSetMDP(gamma = 1.0,
                          state_dim = Dim(goal_coin + 1),
                          action_dim = { Dim(min(it[0], goal_coin - it[0])) })
        mdp.apply {
            for (s in states) {

            }
        }
        return mdp
    }
}