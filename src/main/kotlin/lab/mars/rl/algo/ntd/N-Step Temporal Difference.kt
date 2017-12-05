package lab.mars.rl.algo.ntd

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.collection.IndexedCollection
import org.slf4j.LoggerFactory

const val MAX_N = 1024

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class NStepTemporalDifference(val indexedMdp: IndexedMDP, val n: Int, var π: IndexedPolicy = null_policy) {
  companion object {
    val log = LoggerFactory.getLogger(this::class.java)!!
  }

  val started = indexedMdp.started
  val states = indexedMdp.states
  var episodes = 10000
  val γ = indexedMdp.γ
  var α = 0.1
  var ε = 0.1
  var σ: (Int) -> Int = { 0 }

  var episodeListener: (Int, IndexedCollection<Double>) -> Unit = { _, _ -> }
}