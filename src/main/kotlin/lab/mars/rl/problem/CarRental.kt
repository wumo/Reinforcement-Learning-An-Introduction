package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.CNSetMDP
import lab.mars.rl.util.dimension.cnsetFrom
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.math.poisson
import org.apache.commons.math3.util.FastMath.*

/**
 * <p>
 * Created on 2017-09-06.
 * </p>
 *
 * @author wumo
 */
object CarRental {
    const val max_car = 20
    private const val max_move = 5
    private const val cost_per_car_moved = 2.0
    private const val credit_per_car_rent = 10.0
    private const val max_car_per_parking_lot = 10
    private const val cost_per_parking_lot = 4.0
    private const val mean_for_rent_L1 = 3.0
    private const val mean_for_rent_L2 = 4.0
    private const val mean_for_return_L1 = 3.0
    private const val mean_for_return_L2 = 2.0
    private val lambda = DoubleArray(4)
    private val prob = Array(4) { Array(max_car + 1) { DoubleArray(2) } }
    private const val idx_prob_rent_L1 = 0
    private const val idx_prob_rent_L2 = 1
    private const val idx_prob_return_L1 = 2
    private const val idx_prob_return_L2 = 3
    private const val num_idx_prob = 4
    private const val idx_normal = 0
    private const val idx_cumulative = 1

    init {
        lambda[idx_prob_rent_L1] = mean_for_rent_L1
        lambda[idx_prob_rent_L2] = mean_for_rent_L2
        lambda[idx_prob_return_L1] = mean_for_return_L1
        lambda[idx_prob_return_L2] = mean_for_return_L2

        for (L in 0 until num_idx_prob)
            for (k in 0..max_car) {
                prob[L][k][idx_normal] = poisson(lambda[L], k)
                prob[L][k][idx_cumulative] = if (k < 1) 1.0 else prob[L][k - 1][idx_cumulative] - prob[L][k - 1][idx_normal]
            }
    }

    private fun max_move(num_L1: Int, num_L2: Int): Int {
        val max_L1_to_L2 = num_L1 - max(0, num_L1 - max_move)//L1最多能移动的数量
        val accept_L1_to_L2 = min(max_car, num_L2 + max_move) - num_L2//L2最多能接受的数量，超过20无增益。
        return min(max_L1_to_L2, accept_L1_to_L2)
    }

    fun make(exercise4_4_version: Boolean): IndexedMDP {
        val mdp = CNSetMDP(gamma = 0.9, state_dim = (max_car + 1) x (max_car + 1)) { idx ->
            val max_L1_to_L2 = max_move(idx[0], idx[1])
            val max_L2_to_L1 = max_move(idx[1], idx[0])
            max_L1_to_L2 + max_L2_to_L1 + 1
        }
        for (s in mdp.states) {
            val s_1 = s[0]
            val s_2 = s[1]
            val max_L1_to_L2 = max_move(s_1, s_2)
            for (action in s.actions) {
                val idx = action[0]
                val L1_to_L2 = max_L1_to_L2 - idx
                val nL1 = s_1 - L1_to_L2
                val nL2 = s_2 + L1_to_L2
                val possibles = cnsetFrom((max_car + 1) x (max_car + 1) x { min(it[0], nL1) + min(it[1], nL2) + 1 }) { null_possible }
                val cost = if (exercise4_4_version) {
                    val move_cost = (if (L1_to_L2 >= 1) L1_to_L2 - 1 else abs(L1_to_L2)) * cost_per_car_moved
                    val parking_cost = (ceil(nL1.toDouble() / max_car_per_parking_lot) - 1 + ceil(nL2.toDouble() / max_car_per_parking_lot) - 1) * cost_per_parking_lot
                    //                        double parking_cost=0;
                    move_cost + parking_cost
                } else
                    abs(L1_to_L2) * cost_per_car_moved
                for (rent_L1 in 0..nL1)
                    for (rent_L2 in 0..nL2) {
                        val _prob = prob[idx_prob_rent_L1][rent_L1][if (rent_L1 < nL1) idx_normal else idx_cumulative] * prob[idx_prob_rent_L2][rent_L2][if (rent_L2 < nL2) idx_normal else idx_cumulative]
                        val total_rent = rent_L1 + rent_L2
                        val reward = total_rent * credit_per_car_rent - cost
                        val max_return_L1 = max_car - (nL1 - rent_L1)
                        val max_return_L2 = max_car - (nL2 - rent_L2)
                        for (return_L1 in 0..max_return_L1)
                            for (return_L2 in 0..max_return_L2) {
                                var _prob2 = _prob
                                _prob2 *= prob[idx_prob_return_L1][return_L1][if (return_L1 < max_return_L1) idx_normal else idx_cumulative] * prob[idx_prob_return_L2][return_L2][if (return_L2 < max_return_L2) idx_normal else idx_cumulative]
                                val new_L1 = nL1 - rent_L1 + return_L1
                                val new_L2 = nL2 - rent_L2 + return_L2
                                val min_rent = max(0, nL1 - new_L1) + max(0, nL2 - new_L2)
                                var possible = possibles[new_L1, new_L2, total_rent - min_rent]
                                if (possible === null_possible) {
                                    possible = IndexedPossible(mdp.states[new_L1, new_L2], reward, _prob2)
                                    possibles[new_L1, new_L2, total_rent - min_rent] = possible
                                } else
                                    possible.probability += _prob2
                            }
                    }

                action.possibles = possibles
            }
        }

        return mdp

    }
}
