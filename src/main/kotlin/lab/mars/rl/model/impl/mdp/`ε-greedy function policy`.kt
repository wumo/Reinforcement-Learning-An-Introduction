package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.*
import lab.mars.rl.util.math.*

class `ε-greedy function policy`(val q: ActionValueApproxFunction, val ε: Double) : Policy {
    override fun invoke(s: State): Action<State> {
        return if (Rand().nextDouble() < ε)
            s.actions.rand()
        else
            argmax_tie_random(s.actions) { q(s, it) }
    }

    override fun get(s: State, a: Action<State>): Double {
        val (m, c) = max_count(s.actions) { q(s, it) }
        return if (q(s, a) == m) (1.0 / c - ε / c - ε / s.actions.size) else ε / s.actions.size
    }

}