package lab.mars.rl.algo.ntd

import lab.mars.rl.algo.V_from_Q_ND
import lab.mars.rl.model.*
import lab.mars.rl.util.*
import lab.mars.rl.util.buf.newBuf
import org.apache.commons.math3.util.FastMath.min
import org.apache.commons.math3.util.FastMath.pow
import org.slf4j.LoggerFactory

const val MAX_N = 1024

/**
 * <p>
 * Created on 2017-10-09.
 * </p>
 *
 * @author wumo
 */
class nStepTemporalDifference(val mdp: MDP, val n: Int, var initial_policy: NonDeterminedPolicy = emptyNSet()) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java)!!
    }

    val gamma = mdp.gamma
    val started = mdp.started
    val states = mdp.states
    var episodes = 10000
    var alpha = 0.1
    var epsilon = 0.1

    internal fun updatePolicy(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
        val `a*` = argmax(s.actions) { Q[s, it] }
        val size = s.actions.size
        for (a in s.actions) {
            policy[s, a] = when {
                a === `a*` -> 1.0 - epsilon + epsilon / size
                else -> epsilon / size
            }
        }
    }

    fun QLearning(): OptimalSolution {
        TODO()
    }

    fun expectedSarsa(): OptimalSolution {
        TODO()
    }

    fun DoubleQLearning(): OptimalSolution {
        TODO()
    }

    var sig: (Int) -> Int = { 0 }

}