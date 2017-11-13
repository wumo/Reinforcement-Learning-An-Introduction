package lab.mars.rl.algo.approximation

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.debug
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

class `Semi-gradient TD(0)`(val mdp: MDP, var policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var epsilon = 0.1
    var alpha = 2e-5

    fun prediction(v: ValueFunction) {
        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            var s = started.rand()
            while (s.isNotTerminal()) {
                val a = s.actions.rand(policy(s))
                val (s_next, reward, _) = a.sample()
                v.update(s, alpha * (reward + gamma * v[s_next] - v[s]))
                s = s_next
            }
        }
    }
}