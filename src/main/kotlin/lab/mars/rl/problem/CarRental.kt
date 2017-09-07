package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.NSetMDP
import org.apache.commons.math3.util.FastMath.max
import org.apache.commons.math3.util.FastMath.min

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
object CarRental {
    const val max_car = 20
    const val max_move = 5
    const val cost_per_car_moved = 2.0
    const val credit_per_car_rent = 10.0
    const val max_car_per_parking_lot = 10
    const val cost_per_parking_lot = 4.0
    const val mean_for_rent_L1 = 3.0
    const val mean_for_rent_L2 = 4.0
    const val mean_for_return_L1 = 3.0
    const val mean_for_return_L2 = 2.0
    const val gamma = 0.9
    private val lambda = DoubleArray(4)
    private val prob = Array(4) { Array(max_car + 1) { DoubleArray(2) } }
    private const val idx_prob_rent_L1 = 0
    private const val idx_prob_rent_L2 = 1
    private const val idx_prob_return_L1 = 2
    private const val idx_prob_return_L2 = 3
    private const val num_idx_prob = 4
    private const val idx_normal = 0
    private const val idx_cumulative = 1

    private fun max_move(num_L1: Int, num_L2: Int): Int {
        val max_L1_to_L2 = num_L1 - max(0, num_L1 - max_move)//L1最多能移动的数量
        val accept_L1_to_L2 = min(max_car, num_L2 + max_move) - num_L2//L2最多能接受的数量，超过20无增益。
        return min(max_L1_to_L2, accept_L1_to_L2)
    }

    fun make(): MDP {
        val mdp = NSetMDP(state_dim = intArrayOf(max_car + 1, max_car + 1),
                action_dim = intArrayOf(max_move + max_move + 1), gamma = 0.9)// 因为我们使用的是确定策略，但是GridWorld问题中存在确定策略的无限循环，此时便不是episode mdp，gamma必须小于1
//        mdp.apply {
//            for (s in states) {
//                s!!
//                val max_L1_to_L2 = max_move(s.idx[0], s.idx[1])
//                val max_L2_to_L1 = max_move(s.idx[1], s.idx[0])
//                val total_move_action = max_L1_to_L2 + max_L2_to_L1 + 1
//                s!!.actions = DimNSet(GridWorld.action_num) {
//                    val action_idx = it[0]
//                    val action = Action(action_idx)
//                    var x = s.idx[0] + GridWorld.move[action_idx][0]
//                    var y = s.idx[1] + GridWorld.move[action_idx][1]
//                    if (x < 0 || x >= GridWorld.n || y < 0 || y >= GridWorld.n) {
//                        x = s.idx[0]
//                        y = s.idx[1]
//                    }
//                    action.possibles = DimNSet(1) { Possible(states[x, y]!!, -1.0, 1.0) }
//                    action
//                }
//            }
//            states[0, 0]!!.actions = emptyActions
//            states[GridWorld.n - 1, GridWorld.n - 1]!!.actions = emptyActions
//        }

        return mdp

    }
}