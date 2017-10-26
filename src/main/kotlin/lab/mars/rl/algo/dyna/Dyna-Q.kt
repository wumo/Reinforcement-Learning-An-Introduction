package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy`
import lab.mars.rl.model.*
import lab.mars.rl.util.*
import org.slf4j.LoggerFactory

interface Environment {
    fun current(): State
    fun reset()
}

@Suppress("NAME_SHADOWING")
class DynaQ(val mdp: MDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var stepListener: (ActionValueFunction, State) -> Unit = { _, _ -> }
    var episodeListener: (StateValueFunction) -> Unit = {}

    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1
    var n = 10

    fun optimal(_alpha: (State, Action) -> Double = { _, _ -> alpha }): OptimalSolution {
        val policy = mdp.QFunc { 0.0 }
        val Q = mdp.QFunc { Rand().nextDouble(1.0) }
        val Model = HashMapRAC<tuple4<State, Action, Double, State>>()
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var count = 0
            var s = started.rand()
            while (s.isNotTerminal()) {
                V_from_Q_ND(states, result)
                stepListener(V, s)
                count++
                `e-greedy`(s, Q, policy, epsilon)
                val a = s.actions.rand(policy(s))
                val (s_next, reward, _) = a.sample()
                Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                Model[s, a] = tuple4(s, a, reward, s_next)
                repeat(n) {
                    val (s, a, reward, s_next) = Model.rand()
                    Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                }
                s = s_next
                episodeListener(V)
            }
            log.debug { "steps=$count" }
        }
        return result
    }
}