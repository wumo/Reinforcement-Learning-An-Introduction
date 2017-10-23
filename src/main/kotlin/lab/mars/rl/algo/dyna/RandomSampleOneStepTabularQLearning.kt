package lab.mars.rl.algo.dyna

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.algo.`e-greedy`
import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.OptimalSolution
import lab.mars.rl.model.State
import lab.mars.rl.util.debug
import lab.mars.rl.util.max
import org.slf4j.LoggerFactory

class RandomSampleOneStepTabularQLearning(val mdp: MDP) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1

    fun optimal(_alpha: (State, Action) -> Double = { _, _ -> alpha }): OptimalSolution {
        val Q = mdp.QFunc { 0.0 }
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            var a = s.actions.rand()//Exploring Starts
            val possible = a.sample()
            val s_next = possible.next
            if (s_next.isNotTerminal())
                Q[s, a] += _alpha(s, a) * (possible.reward + gamma * max(s_next.actions) { Q[s_next, it] } - Q[s, a])
            else
                Q[s, a] += _alpha(s, a) * (possible.reward + gamma * 0.0 - Q[s, a])//Q[terminalState,*]=0.0
        }
        val policy = mdp.QFunc { 0.0 }
        for (s in states) {
            if (s.isTerminal()) continue
            `e-greedy`(s, Q, policy, epsilon)
        }
        val V = mdp.VFunc { 0.0 }
        val result = Triple(policy, V, Q)
        V_from_Q_ND(states, result)
        return result
    }
}

