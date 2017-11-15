package lab.mars.rl.algo.func_approx

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

class FunctionApprox(val mdp: MDP, var `π`: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val started = mdp.started
    var episodes = 10000
    val `γ` = mdp.`γ`
    var `α` = 1.0

    var episodeListener: (Int) -> Unit = {}
}