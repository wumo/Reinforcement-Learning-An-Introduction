package lab.mars.rl.algo.td

import lab.mars.rl.model.impl.mdp.*
import org.slf4j.LoggerFactory

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class TemporalDifference(val indexedMdp: IndexedMDP, var π: IndexedPolicy = null_policy) {
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