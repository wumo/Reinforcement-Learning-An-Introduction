@file:Suppress("UNCHECKED_CAST")

package lab.mars.rl.model.impl.mdp

import lab.mars.rl.model.MDP
import lab.mars.rl.model.isTerminal
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.collection.IndexedCollection

/**
 *
 * @property states 状态集
 * @property γ 衰减因子
 * @property state_function 状态V函数的构造器（不同的[states]实现对应着不同的[state_function]）
 * @property state_action_function 状态动作Q函数的构造器（不同的[states]和[IndexedAction]实现实现对应着不同的[state_action_function]）
 */
class IndexedMDP(
  override val γ: Double,
  val states: StateSet,
  private val state_function: ((Index) -> Any) -> IndexedCollection<Any>,
  private val state_action_function: ((Index) -> Any) -> IndexedCollection<Any>) : MDP {
  override var started = { states.rand() }
  /**
   * 创建由[IndexedState]索引的state function
   *
   * create state function indexed by [IndexedState]
   */
  fun <T : Any> VFunc(element_maker: (Index) -> T) =
    state_function(element_maker) as IndexedCollection<T>

  /**
   * 创建由[IndexedState]和[IndexedAction]索引的state action function
   *
   * create state action function indexed by [IndexedState] and [IndexedAction]
   */
  fun <T : Any> QFunc(element_maker: (Index) -> T) =
    state_action_function(element_maker) as IndexedCollection<T>

  /**
   * equiprobable random policy
   */
  fun equiprobablePolicy(): IndexedPolicy {
    val policy = QFunc { 0.0 }
    for (s in states) {
      if (s.isTerminal) continue
      val prob = 1.0 / s.actions.size
      for (a in s.actions)
        policy[s, a] = prob
    }
    return IndexedPolicy(policy)
  }
}