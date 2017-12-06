package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.*
import lab.mars.rl.util.collection.IndexedCollection
import lab.mars.rl.util.collection.emptyNSet

class IndexedPolicy(val p: IndexedCollection<Double>, val ε: Double = 0.1) : Policy {
  override fun invoke(s: State): IndexedAction {
    val eval = p(s as IndexedState)
    return s.actions.rand { eval[it] }
  }

  override fun get(s: State, a: Action<State>)
    = p[s as IndexedState, a as IndexedAction]

  operator fun set(s: IndexedState, a: IndexedAction, v: Double) {
    p[s, a] = v
  }

  operator fun set(s: IndexedState, newaction: IndexedAction) {
    for (a in s.actions)
      p[s, a] = 0.0
    p[s, newaction] = 1.0
  }
}

val null_policy = IndexedPolicy(emptyNSet())