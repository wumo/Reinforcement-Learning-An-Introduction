package lab.mars.rl.algo

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.util.emptyNSet

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class TemporalDifference(val mdp: MDP, private var policy: NonDeterminedPolicy = emptyNSet()) {
    fun prediction() {
        val V = mdp.VFunc { 0.0 }

    }
}