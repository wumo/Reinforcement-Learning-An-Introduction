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
 * @param gamma `γ` 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 统一的动作维度，Q函数与状态集和动作集一致
 * @return 所有状态维度相同和动作维度相同的MDP实例
 */
inline fun NSetMDP(gamma: Double, state_dim: Any, action_dim: Any): IndexedMDP {
    val a_dim = action_dim.toDim()
    return NSetMDP(gamma, state_dim.toDim(), { a_dim })
}

/**
 * @param gamma  `γ` 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 依据状态索引确定动作维度，Q函数与状态集和动作集一致
 * @return 统一状态维度而动作维度异构的MDP实例
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
 *  注意：维度不能为0，如果需要为0，则需要在构建完成后，手动设定`emptyNSet()`
 * @param gamma `γ` 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 统一的动作维度，Q函数与状态集和动作集一致
 * @return 所有状态维度相同和动作维度相同的MDP实例
 */
inline fun CNSetMDP(gamma: Double, state_dim: Any, action_dim: Any): IndexedMDP {
    val a_dim = action_dim.toDim() as GeneralDimension
    return CNSetMDP(gamma, state_dim.toDim(), { a_dim })
}

/**
 * 注意：维度不能为0，如果需要为0，则需要在构建完成后，手动设定`emptyNSet()`
 * @param gamma  `γ` 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 依据状态索引确定动作维度，Q函数与状态集和动作集一致
 * @return 统一状态维度而动作维度异构的MDP实例
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