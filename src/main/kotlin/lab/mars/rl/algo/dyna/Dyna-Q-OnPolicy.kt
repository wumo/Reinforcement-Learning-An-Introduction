package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy (tie broken randomly)`
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.tuples.tuple3
import org.slf4j.LoggerFactory
import java.util.*

@Suppress("NAME_SHADOWING")
class `Dyna-Q-OnPolicy`(val indexedMdp: IndexedMDP) {
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

    fun optimal(α: (IndexedState, IndexedAction) -> Double = { _, _ -> this.α }): OptimalSolution {
        val π = IndexedPolicy(indexedMdp.QFunc { 0.0 })
        val Q = indexedMdp.QFunc { 0.0 }
        val V = indexedMdp.VFunc { 0.0 }
        val result = tuple3(π, V, Q)

        val startedStates = hashMapOf<IndexedState, Int>()
        val Model = indexedMdp.QFunc { hashMapOf<tuple2<IndexedState, Double>, Int>() }
        val N = indexedMdp.QFunc { 0 }

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var step = 0
            var stat = 0
            var s = started()
            startedStates.compute(s) { _, v -> (v ?: 0) + 1 }//record the total visits of each state
            while (s.isNotTerminal()) {
                V_from_Q(states, result)
                stepListener(V, s)
                step++
                `ε-greedy (tie broken randomly)`(s, Q, π, ε)
                val a = π(s)
                val (s_next, reward) = a.sample()
                Model[s, a].compute(tuple2(s_next, reward)) { _, v -> (v ?: 0) + 1 }
                N[s, a]++
                Q[s, a] += α(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])

                var _s = startedStates.rand(episode)
                lab.mars.rl.util.math.repeat(n, { _s.isNotTerminal() }) {
                    `ε-greedy (tie broken randomly)`(_s, Q, π, ε)//using on-policy to distribute computation
                    val a = π(_s)
                    if (Model[_s, a].isEmpty()) return@repeat
                    stat++
                    val (s_next, reward) = Model[_s, a].rand(N[_s, a])
                    Q[_s, a] += α(_s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[_s, a])
                    _s = s_next
                }
                s = s_next
            }
            episodeListener(V)
            log.debug { "steps=$step, stat=$stat" }
        }
        return result
    }

    fun <K> HashMap<K, Int>.rand(N: Int): K {
        val p = Rand().nextDouble()
        var acc = 0.0
        for ((k, v) in this) {
            acc += v.toDouble() / N
            if (p <= acc)
                return k
        }
        throw IllegalArgumentException("random=$p, but accumulation=$acc")
    }
}