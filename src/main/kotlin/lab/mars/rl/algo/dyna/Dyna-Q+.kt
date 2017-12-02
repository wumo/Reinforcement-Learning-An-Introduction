package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.null_state
import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.tuples.tuple3
import org.apache.commons.math3.util.FastMath.sqrt
import org.slf4j.LoggerFactory

@Suppress("NAME_SHADOWING")
class `Dyna-Q+`(val indexedMdp: IndexedMDP) {
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
    var κ = 1e-4
    var n = 10
    val null_tuple3 = tuple3(null_state, Double.NaN, 0)
    fun optimal(α: (IndexedState, IndexedAction) -> Double = { _, _ -> this.α }): OptimalSolution {
        val π = IndexedPolicy(indexedMdp.QFunc { 0.0 })
        val Q = indexedMdp.QFunc { 0.0 }
        val cachedSA = DefaultBuf.new<tuple2<IndexedState, IndexedAction>>(Q.size)
        val Model = indexedMdp.QFunc { null_tuple3 }
        val V = indexedMdp.VFunc { 0.0 }
        val result = tuple3(π, V, Q)
        var time = 0
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started()
            while (s.isNotTerminal()) {
                V_from_Q(states, result)
                stepListener(V, s)
                time++
                `ε-greedy`(s, Q, π, ε)
                val a = π(s)
                val (s_next, reward) = a.sample()
                Q[s, a] += α(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                for (_a in s.actions) {
                    if (_a !== a && Model[s, _a] === null_tuple3) {
                        cachedSA.append(tuple2(s, _a))
                        Model[s, _a] = tuple3(s, 0.0, 1)
                    }
                }
                if (Model[s, a] === null_tuple3)
                    cachedSA.append(tuple2(s, a))
                Model[s, a] = tuple3(s_next, reward, time)
                repeat(n) {
                    val (s, a) = cachedSA.rand()
                    var (s_next, reward, t) = Model[s, a]
                    reward += κ * sqrt((time - t).toDouble())
                    Q[s, a] += α(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                }
                s = s_next
            }
            episodeListener(V)
            log.debug { "steps=$time" }
        }
        return result
    }
}