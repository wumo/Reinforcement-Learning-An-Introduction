package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.util.NSet
import lab.mars.rl.model.impl.NSetMDP
import lab.mars.rl.util.x
import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.util.FastMath.*
import org.apache.commons.math3.util.MathUtils

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

    fun make(exercise4_4_version: Boolean): MDP {
        val mdp = NSetMDP(gamma = 0.9, state_dim = (max_car + 1) x (max_car + 1)) { idx ->
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
                val possibles = NSet<Possible>((max_car + 1) x (max_car + 1))
                for (_L1 in 0..max_car)
                    for (_L2 in 0..max_car)
                        possibles[_L1, _L2] = NSet(min(_L1, nL1) + min(_L2, nL2) + 1)
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
                                var possible: Possible? = possibles[new_L1, new_L2, total_rent - min_rent]
                                if (possible === null) {
                                    possible = Possible(mdp.states[new_L1, new_L2], reward, _prob2)
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

private fun poisson(mean: Double, n: Int): Double {
    //        if (n > 11) return 0;
    //        return exp(-mean) * pow(mean, n) / CombinatoricsUtils.factorial(n);
    val ret: Double
    if (n < 0 || n == Int.MAX_VALUE)
        ret = Double.NEGATIVE_INFINITY
    else if (n == 0)
        ret = -mean
    else
        ret = -getStirlingError(n.toDouble()) - getDeviancePart(n.toDouble(), mean) - 0.5 * log(MathUtils.TWO_PI) - 0.5 * log(n.toDouble())
    return if (ret == Double.NEGATIVE_INFINITY) 0.0 else exp(ret)
}

/** 1/2 * log(2 &#960;).  */
private val HALF_LOG_2_PI = 0.5 * log(MathUtils.TWO_PI)

/** exact Stirling expansion error for certain values.  */
private val EXACT_STIRLING_ERRORS = doubleArrayOf(0.0, /* 0.0 */
                                                  0.1534264097200273452913848, /* 0.5 */
                                                  0.0810614667953272582196702, /* 1.0 */
                                                  0.0548141210519176538961390, /* 1.5 */
                                                  0.0413406959554092940938221, /* 2.0 */
                                                  0.03316287351993628748511048, /* 2.5 */
                                                  0.02767792568499833914878929, /* 3.0 */
                                                  0.02374616365629749597132920, /* 3.5 */
                                                  0.02079067210376509311152277, /* 4.0 */
                                                  0.01848845053267318523077934, /* 4.5 */
                                                  0.01664469118982119216319487, /* 5.0 */
                                                  0.01513497322191737887351255, /* 5.5 */
                                                  0.01387612882307074799874573, /* 6.0 */
                                                  0.01281046524292022692424986, /* 6.5 */
                                                  0.01189670994589177009505572, /* 7.0 */
                                                  0.01110455975820691732662991, /* 7.5 */
                                                  0.010411265261972096497478567, /* 8.0 */
                                                  0.009799416126158803298389475, /* 8.5 */
                                                  0.009255462182712732917728637, /* 9.0 */
                                                  0.008768700134139385462952823, /* 9.5 */
                                                  0.008330563433362871256469318, /* 10.0 */
                                                  0.007934114564314020547248100, /* 10.5 */
                                                  0.007573675487951840794972024, /* 11.0 */
                                                  0.007244554301320383179543912, /* 11.5 */
                                                  0.006942840107209529865664152, /* 12.0 */
                                                  0.006665247032707682442354394, /* 12.5 */
                                                  0.006408994188004207068439631, /* 13.0 */
                                                  0.006171712263039457647532867, /* 13.5 */
                                                  0.005951370112758847735624416, /* 14.0 */
                                                  0.005746216513010115682023589, /* 14.5 */
                                                  0.005554733551962801371038690 /* 15.0 */)

/**
 * Compute the error of Stirling's series at the given value.
 * References:
 *
 *  1. Eric W. Weisstein. "Stirling's Series." From MathWorld--A Wolfram Web
 * Resource. [
 * http://mathworld.wolfram.com/StirlingsSeries.html](http://mathworld.wolfram.com/StirlingsSeries.html)
 *
 * @param z
 * the value.
 * @return the Striling's series error.
 */
fun getStirlingError(z: Double): Double {
    val ret: Double
    ret = if (z < 15.0) {
        val z2 = 2.0 * z
        if (floor(z2) == z2) {
            EXACT_STIRLING_ERRORS[z2.toInt()]
        } else {
            Gamma.logGamma(z + 1.0) - (z + 0.5) * log(z) + z - HALF_LOG_2_PI
        }
    } else {
        val z2 = z * z
        (0.083333333333333333333 - (0.00277777777777777777778 - (0.00079365079365079365079365 - (0.000595238095238095238095238 - 0.0008417508417508417508417508 / z2) / z2) / z2) / z2) / z
    }
    return ret
}

/**
 * A part of the deviance portion of the saddle point approximation.
 *
 * References:
 *
 *  1. Catherine Loader (2000). "Fast and Accurate Computation of Binomial
 * Probabilities.". [
 * http://www.herine.net/stat/papers/dbinom.pdf](http://www.herine.net/stat/papers/dbinom.pdf)
 *
 * @param x
 * the e value.
 * @param mu
 * the average.
 * @return a part of the deviance.
 */
fun getDeviancePart(x: Double, mu: Double): Double {
    val ret: Double
    if (abs(x - mu) < 0.1 * (x + mu)) {
        val d = x - mu
        var v = d / (x + mu)
        var s1 = v * d
        var s = Double.NaN
        var ej = 2.0 * x * v
        v *= v
        var j = 1
        while (s1 != s) {
            s = s1
            ej *= v
            s1 = s + ej / (j * 2 + 1)
            ++j
        }
        ret = s1
    } else {
        ret = x * log(x / mu) + mu - x
    }
    return ret
}