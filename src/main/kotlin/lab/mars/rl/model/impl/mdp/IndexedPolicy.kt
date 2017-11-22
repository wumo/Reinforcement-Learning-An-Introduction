package lab.mars.rl.model.impl.mdp

import lab.mars.rl.algo.`ε-greedy`
import lab.mars.rl.model.*
import lab.mars.rl.util.collection.*

class IndexedPolicy(val p: IndexedCollection<Double>, val ε: Double = 0.1) : Policy {
    override fun invoke(s: State): IndexedAction = (s as IndexedState).actions.rand(p(s))

    override fun get(s: State, a: Action<State>)
            = p[s as IndexedState, a as IndexedAction]

    override fun `ε-greedy update`(s: State, evaluate: Gettable<Action<State>, Double>, ε: Double)
            = `ε-greedy`(s as IndexedState, evaluate, this, ε)

    operator fun set(s: IndexedState, a: IndexedAction, v: Double) {
        p[s, a] = v
    }

    fun deteministic(s: IndexedState, newaction: IndexedAction) {
        for (a in s.actions)
            p[s, a] = 0.0
        p[s, newaction] = 1.0
    }
}

val null_policy = IndexedPolicy(emptyNSet())