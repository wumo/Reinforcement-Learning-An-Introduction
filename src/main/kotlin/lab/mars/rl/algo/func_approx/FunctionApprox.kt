package lab.mars.rl.algo.func_approx

import lab.mars.rl.model.IndexedMDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

class FunctionApprox(val indexedMdp: IndexedMDP, var π: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val started = indexedMdp.started
    var episodes = 10000
    val γ = indexedMdp.γ
    var α = 1.0
    var ε = 0.1
    
    var episodeListener: (Int) -> Unit = {}
}