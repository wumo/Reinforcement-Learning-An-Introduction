@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.MDP
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.collection.IndexedCollection

/**
 *
 * @property states state set
 * @property γ decay factor
 * @property state_function [state_function] generator
 * @property state_action_function [state_action_function] generator
 */
class IndexedMDP(
    override val γ: Double,
    val states: StateSet,
    private val state_function: ((Index) -> Any) -> IndexedCollection<Any>,
    private val state_action_function: ((Index) -> Any) -> IndexedCollection<Any>): MDP {
  override var started = { states.rand() }
  /**
   *
   * create state function indexed by [IndexedState]
   */
  fun <T: Any> VFunc(element_maker: (Index) -> T) =
      state_function(element_maker) as IndexedCollection<T>
  
  /**
   *
   * create state action function indexed by [IndexedState] pair [IndexedAction]
   */
  fun <T: Any> QFunc(element_maker: (Index) -> T) =
      state_action_function(element_maker) as IndexedCollection<T>
  
  /**
   * equiprobable random policy
   */
  fun equiprobablePolicy(): IndexedPolicy {
    val policy = QFunc { 0.0 }
    for (s in states.filter { it.isNotTerminal }) {
      val prob = 1.0 / s.actions.size
      for (a in s.actions)
        policy[s, a] = prob
    }
    return IndexedPolicy(policy)
  }
}