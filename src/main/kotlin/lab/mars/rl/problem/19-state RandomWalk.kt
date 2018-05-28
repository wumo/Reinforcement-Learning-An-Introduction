package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.CNSetMDP
import lab.mars.rl.model.impl.mdp.IndexedPolicy
import lab.mars.rl.model.impl.mdp.IndexedPossible
import lab.mars.rl.util.collection.cnsetOf
import lab.mars.rl.util.collection.emptyNSet

/**
 * <p>
 * Created on 2017-10-10.
 * </p>
 *
 * @author wumo
 */
object `19-state RandomWalk` {
  val num_states = 19
  fun make(): IndexedProblem {
    val mdp = CNSetMDP(1.0, num_states + 2, 1)
    mdp.apply {
      val last = num_states + 1
      states[0].actions = emptyNSet()
      states[last].actions = emptyNSet()
      started = { states((num_states + 1) / 2).rand() }
      for (a in 1 until last) {
        states[a].actions[0].apply {
          val left = a - 1
          val right = a + 1
          possibles = cnsetOf(IndexedPossible(states[left], if (left == 0) -1.0 else 0.0, 0.5),
                              IndexedPossible(states[right], if (right == last) 1.0 else 0.0, 0.5))
        }
      }
    }
    val policy = mdp.QFunc { 1.0 }
    return Pair(mdp, IndexedPolicy(policy))
  }
}