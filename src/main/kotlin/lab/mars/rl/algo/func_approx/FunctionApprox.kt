package lab.mars.rl.algo.func_approx

import lab.mars.rl.model.MDP
import lab.mars.rl.model.Policy
import org.slf4j.LoggerFactory

class FunctionApprox(val mdp: MDP, var π: Policy) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val started = mdp.started
    var episodes = 10000
    val γ = mdp.γ
    var α = 1.0
    var ε = 0.1

    var episodeListener: (Int) -> Unit = {}
}