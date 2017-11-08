package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy tie random`
import lab.mars.rl.model.*
import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.cnsetOf
import lab.mars.rl.util.debug
import lab.mars.rl.util.max
import lab.mars.rl.util.tuples.tuple2
import lab.mars.rl.util.tuples.tuple3
import org.slf4j.LoggerFactory

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
        val Q = mdp.QFunc { 0.0 }
        val cachedSA = DefaultBuf.new<tuple2<State, Action>>(Q.size)
        val Model = mdp.QFunc { emptyPossibleSet }
        val V = mdp.VFunc { 0.0 }
        val result = tuple3(policy, V, Q)
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var step = 0
            var s = started.rand()
            while (s.isNotTerminal()) {
                V_from_Q_ND(states, result)
                stepListener(V, s)
                step++
                `e-greedy tie random`(s, Q, policy, epsilon)
                val a = s.actions.rand(policy(s))
                val (s_next, reward, _) = a.sample()
                Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                if (Model[s, a].isEmpty())
                    cachedSA.append(tuple2(s, a))
                Model[s, a] = cnsetOf(Possible(s_next, reward, 1.0))
                repeat(n) {
                    val (s, a) = cachedSA.rand()
                    val (s_next, reward) = Model[s, a].rand()
                    Q[s, a] += _alpha(s, a) * (reward + gamma * max(s_next.actions, 0.0) { Q[s_next, it] } - Q[s, a])
                }
                s = s_next

            }
            episodeListener(V)
            log.debug { "steps=$step" }
        }
        return result
    }
}