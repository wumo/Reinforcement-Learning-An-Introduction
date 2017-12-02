package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.max
import lab.mars.rl.util.tuples.tuple3
import org.slf4j.LoggerFactory

class RandomSampleOneStepTabularQLearning(val indexedMdp: IndexedMDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val γ = indexedMdp.γ
    val started = indexedMdp.started
    val states = indexedMdp.states
    var episodes = 10000
    var α = 0.1
    var ε = 0.1

    fun optimal(α: (IndexedState, IndexedAction) -> Double = { _, _ -> this.α }): OptimalSolution {
        val Q = indexedMdp.QFunc { 0.0 }
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            val s = started()
            val a = s.actions.rand()//Exploring Starts
            val (s_next, reward) = a.sample()
            Q[s, a] += α(s, a) * (reward + γ * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
        }
        val π = IndexedPolicy(indexedMdp.QFunc { 0.0 })
        for (s in states) {
            if (s.isTerminal()) continue
            `ε-greedy`(s, Q, π, ε)
        }
        val V = indexedMdp.VFunc { 0.0 }
        val result = tuple3(π, V, Q)
        V_from_Q(states, result)
        return result
    }
}

