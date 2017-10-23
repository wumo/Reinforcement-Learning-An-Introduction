package lab.mars.rl.algo.mc

import lab.mars.rl.algo.mc.MonteCarlo.Companion.log
import lab.mars.rl.model.StateValueFunction
import lab.mars.rl.util.debug

fun MonteCarlo.prediction(): StateValueFunction {
    val V = mdp.VFunc { 0.0 }
    val preReturn = mdp.VFunc { Double.NaN }
    val count = mdp.VFunc { 0 }

    for (episode in 1..episodes) {
        log.debug { "$episode/$episodes" }
        var s = started.rand()
        var accumulate = 0.0
        while (s.isNotTerminal()) {
            val a = s.actions.rand(policy(s))
            val possible = a.sample()
            if (preReturn[s].isNaN())
                preReturn[s] = accumulate
            accumulate += possible.reward
            s = possible.next
        }
        preReturn.set { idx, value ->
            if (!value.isNaN()) {
                V[idx] += accumulate - value
                count[idx] += 1
            }
            Double.NaN
        }
    }
    for (s in states) {
        val n = count[s]
        if (n > 0)
            V[s] = V[s] / n
    }
    return V
}