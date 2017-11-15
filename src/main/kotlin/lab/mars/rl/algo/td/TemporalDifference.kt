package lab.mars.rl.algo.td

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class TemporalDifference(val mdp: MDP, var initial_policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    val `γ` = mdp.`γ`
    var `α` = 0.1
    var `ε` = 0.1
}