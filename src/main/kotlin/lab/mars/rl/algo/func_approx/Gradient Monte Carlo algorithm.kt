package lab.mars.rl.algo.func_approx

import lab.mars.rl.model.MDP
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.State
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.buf.newBuf
import lab.mars.rl.util.debug
import lab.mars.rl.util.emptyNSet
import org.slf4j.LoggerFactory

class `Gradient Monte Carlo algorithm`(val mdp: MDP, var policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var epsilon = 0.1
    var alpha = 0.1

    fun prediction(v: ValueFunction) {
        val _S = newBuf<State>()
        val _R = newBuf<Double>()

        for (episode in 1..episodes) {
            log.debug { "$episode/$episodes" }
            _S.clear(); _R.clear()
            var s = started.rand()
            _S.append(s); _R.append(0.0)
            var T = 0
            var accum = 0.0
            while (s.isNotTerminal()) {
                val a = s.actions.rand(policy(s))
                val (s_next, reward, _) = a.sample()
                accum += reward
                _S.append(s_next)
                _R.append(reward)
                s = s_next
                T++
            }
            var pre = 0.0
            for (t in 0 until T) {
                pre += _R[t]
                val Gt = accum - pre
                v.update(_S[t], alpha * (Gt - v[_S[t]]))
            }
        }
    }
}