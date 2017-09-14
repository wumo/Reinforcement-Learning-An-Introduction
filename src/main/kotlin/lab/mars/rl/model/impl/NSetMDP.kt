@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.model.impl

import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.State
import lab.mars.rl.util.IntSlice
import lab.mars.rl.util.ReadOnlyIntSlice

/**
 * <p>
 * Created on 2017-09-14.
 * </p>
 *
 * @author wumo
 */

/**
 * @param gamma gamma 衰减因子
 * @param states 指定状态集，V函数与状态集一致
 * @param action_dim 依据状态索引确定动作维度，Q函数与状态集和动作集一致
 * @return 使用指定状态集，动态动作维度的MDP实例
 */
fun NSetMDP(gamma: Double, states: NSet<State>, action_dim: (ReadOnlyIntSlice) -> IntArray) = MDP(
        states = states,
        gamma = gamma,
        v_maker = { NSet(states) { 0.0 } },
        q_maker = { NSet(states) { NSet.use<Double>(action_dim(it)) { 0.0 } } },
        pi_maker = { NSet(states) })

/**
 * @see NSetMDP
 */
inline fun NSetMDP(gamma: Double, state_dim: Int, action_dim: IntSlice) =
        NSetMDP(gamma, IntSlice.of(state_dim), action_dim)

/**
 * @see NSetMDP
 */
inline fun NSetMDP(gamma: Double, state_dim: IntSlice, action_dim: Int) =
        NSetMDP(gamma, state_dim, IntSlice.of(action_dim))

/**
 * @param gamma gamma 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 统一的动作维度，Q函数与状态集和动作集一致
 * @return 所有状态维度相同和动作维度相同的MDP实例
 */
inline fun NSetMDP(gamma: Double, state_dim: IntSlice, action_dim: IntSlice): MDP {
    val a_dim = action_dim.toIntArray()
    return NSetMDP(gamma, state_dim, { a_dim })
}

/**
 * @see NSetMDP
 */
inline fun NSetMDP(gamma: Double, state_dim: Int, crossinline action_dim: (ReadOnlyIntSlice) -> IntArray) =
        NSetMDP(gamma, IntSlice.of(state_dim), action_dim)

/**
 * @param gamma  gamma 衰减因子
 * @param state_dim 统一的状态维度，V函数与状态集一致
 * @param action_dim 依据状态索引确定动作维度，Q函数与状态集和动作集一致
 * @return 统一状态维度而动作维度异构的MDP实例
 */
inline fun NSetMDP(gamma: Double, state_dim: IntSlice, crossinline action_dim: (ReadOnlyIntSlice) -> IntArray): MDP {
    val s_dim = state_dim.toIntArray()
    return MDP(
            states = NSet.use(s_dim) {
                State(it.toIntArray()).apply { actions = NSet.use(action_dim(it)) { Action(it.toIntArray()) } }
            },
            gamma = gamma,
            v_maker = { NSet.use(s_dim) { 0.0 } },
            q_maker = { NSet.use(s_dim) { NSet.use<Double>(action_dim(it)) { 0.0 } } },
            pi_maker = { NSet.use(s_dim) })
}