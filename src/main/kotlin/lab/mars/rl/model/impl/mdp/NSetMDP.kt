@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.model.impl.mdp

import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.dimension.*

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */

/**
 * @param gamma `γ` decay
 * @param state_dim state dimension
 * @param action_dim action dimension
 * @return mdp with same state dimension and same action dimension
 */
inline fun NSetMDP(gamma: Double, state_dim: Any, action_dim: Any): IndexedMDP {
  val a_dim = action_dim.toDim()
  return NSetMDP(gamma, state_dim.toDim(), { a_dim })
}

/**
 * @param gamma  `γ` decay factor
 * @param state_dim state dimension
 * @param action_dim different action dimension according to specific state
 * @return mdp with same state dimension but different action dimension
 */
fun NSetMDP(gamma: Double, state_dim: Any, action_dim: (IntBuf) -> Any): IndexedMDP {
  val s_dim = state_dim.toDim() as GeneralDimension
  val s_a_dim = s_dim.copy() x action_dim
  return IndexedMDP(
      γ = gamma,
      states = nsetFrom(s_dim) {
        IndexedState(it.copy()).apply { actions = nsetFrom(action_dim(it).toDim()) { IndexedAction(it.copy()) } }
      },
      state_function = { element_maker -> nsetFrom(s_dim, element_maker) },
      state_action_function = { element_maker -> nsetFrom(s_a_dim, element_maker) })
}

/**
 *  Note that: dimension shouldn't be 0. It it needs to be 0, then you can set `emptyNSet()` after the construction.
 * @param gamma `γ` decay factor
 * @param state_dim state dimension
 * @param action_dim action dimension
 * @return mdp with same state dimension and same action dimension
 */
inline fun CNSetMDP(gamma: Double, state_dim: Any, action_dim: Any): IndexedMDP {
  val a_dim = action_dim.toDim() as GeneralDimension
  return CNSetMDP(gamma, state_dim.toDim(), { a_dim })
}

/**
 * Note that: dimension shouldn't be 0. It it needs to be 0, then you can set `emptyNSet()` after the construction.
 * @param gamma  `γ`  decay factor
 * @param state_dim state dimension
 * @param action_dim different action dimension according to specific state
 * @return mdp with same state dimension but different action dimension
 */
fun CNSetMDP(gamma: Double, state_dim: Any, action_dim: (IntBuf) -> Any): IndexedMDP {
  val s_dim = state_dim.toDim() as GeneralDimension
  val states = cnsetFrom(s_dim) {
    IndexedState(it.copy()).apply { actions = cnsetFrom(action_dim(it).toDim()) { IndexedAction(it.copy()) } }
  }
  val s_a_dim = s_dim.copy() x action_dim
  return IndexedMDP(
      γ = gamma,
      states = states,
      state_function = { element_maker -> states.copycat(element_maker) },
      state_action_function = { element_maker -> cnsetFrom(s_a_dim, element_maker) })
}

inline fun mdpOf(gamma: Double, state_dim: Any, action_dim: Any)
    = CNSetMDP(gamma, state_dim, action_dim)

inline fun mdpOf(gamma: Double, state_dim: Any, noinline action_dim: (IntBuf) -> Any)
    = CNSetMDP(gamma, state_dim, action_dim)