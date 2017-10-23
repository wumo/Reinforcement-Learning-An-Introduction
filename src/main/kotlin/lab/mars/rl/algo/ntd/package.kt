package lab.mars.rl.algo.ntd

import lab.mars.rl.model.ActionValueFunction
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.State
import lab.mars.rl.util.argmax

internal fun NStepTemporalDifference.updatePolicy(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
    val `a*` = argmax(s.actions) { Q[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
        policy[s, a] = when {
            a === `a*` -> 1.0 - epsilon + epsilon / size
            else -> epsilon / size
        }
    }
}