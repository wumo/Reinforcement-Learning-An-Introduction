package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy (tie broken randomly)`
import lab.mars.rl.model.emptyPossibleSet
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.tuples.tuple3
import org.slf4j.LoggerFactory

@Suppress("NAME_SHADOWING")
class DynaQ(val indexedMdp: IndexedMDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val γ = indexedMdp.γ
    val started = indexedMdp.started
    val states = indexedMdp.states
    var stepListener: (ActionValueFunction, IndexedState) -> Unit = { _, _ -> }
    var episodeListener: (StateValueFunction) -> Unit = {}

    var episodes = 10000
    var α = 0.1
    var ε = 0.1
    var n = 10
    fun optimal(_alpha: (IndexedState, IndexedAction) -> Double = { _, _ -> α }): OptimalSolution {
        val π = IndexedPolicy(indexedMdp.QFunc { 0.0 })
        val Q = indexedMdp.QFunc { 0.0 }
        val cachedSA = DefaultBuf.new<tuple2<IndexedState, IndexedAction>>(Q.size)
        val Model = indexedMdp.QFunc { emptyPossibleSet }
        val V = indexedMdp.VFunc { 0.0 }
        val result = tuple3(π, V, Q)
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var step = 0
            var s = started()
            while (s.isNotTerminal()) {
                V_from_Q(states, result)
                stepListener(V, s)
                step++
                `ε-greedy (tie broken randomly)`(s, Q, π, ε)
                val a = π(s)
                val (s_next, reward) = a.sample()
                Q[s, a] += _alpha(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                if (Model[s, a].isEmpty())
                    cachedSA.append(tuple2(s, a))
                Model[s, a] = cnsetOf(IndexedPossible(s_next, reward, 1.0))
                repeat(n) {
                    val (s, a) = cachedSA.rand()
                    val (s_next, reward) = Model[s, a].rand()
                    Q[s, a] += _alpha(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                }
                s = s_next

            }
            episodeListener(V)
            log.debug { "steps=$step" }
        }
        return result
    }
}