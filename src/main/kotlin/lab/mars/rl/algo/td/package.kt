package lab.mars.rl.algo.td

import lab.mars.rl.model.ActionValueFunction
import lab.mars.rl.model.NonDeterminedPolicy
import lab.mars.rl.model.State
import lab.mars.rl.util.argmax


fun TemporalDifference.`e-greedy`(s: State, Q: ActionValueFunction, policy: NonDeterminedPolicy) {
    val `a*` = argmax(s.actions) { Q[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
        policy[s, a] = when {
            a === `a*` -> 1 - epsilon + epsilon / size
            else -> epsilon / size
        }
    }
}