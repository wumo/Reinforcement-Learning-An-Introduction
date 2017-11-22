package lab.mars.rl.algo.td

import lab.mars.rl.model.IndexedMDP
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
class TemporalDifference(val indexedMdp: IndexedMDP, var initial_policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val started = indexedMdp.started
    val states = indexedMdp.states
    var episodes = 10000
    val γ = indexedMdp.γ
    var α = 0.1
    var ε = 0.1
}