/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lab.mars.rl.util.math

import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.MathUtils


fun poisson(mean: Double, n: Int): Double {
    //        if (n > 11) return 0;
    //        return exp(-mean) * pow(mean, n) / CombinatoricsUtils.factorial(n);
    val ret: Double
    if (n < 0 || n == Int.MAX_VALUE)
        ret = Double.NEGATIVE_INFINITY
    else if (n == 0)
        ret = -mean
    else
        ret = -getStirlingError(n.toDouble()) - getDeviancePart(n.toDouble(), mean) - 0.5 * FastMath.log(MathUtils.TWO_PI) - 0.5 * FastMath.log(n.toDouble())
    return if (ret == Double.NEGATIVE_INFINITY) 0.0 else FastMath.exp(ret)
}

/** 1/2 * log(2 &#960;).  */
private val HALF_LOG_2_PI = 0.5 * FastMath.log(MathUtils.TWO_PI)

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
 * Compute the error nsetFrom Stirling'asSet series at the given value.
 * References:
 *
 *  1. Eric W. Weisstein. "Stirling'asSet Series." From MathWorld--A Wolfram Web
 * Resource. [
 * http://mathworld.wolfram.com/StirlingsSeries.html](http://mathworld.wolfram.com/StirlingsSeries.html)
 *
 * @param z
 * the value.
 * @return the Striling'asSet series error.
 */
fun getStirlingError(z: Double): Double {
    val ret: Double
    ret = if (z < 15.0) {
        val z2 = 2.0 * z
        if (FastMath.floor(z2) == z2) {
            EXACT_STIRLING_ERRORS[z2.toInt()]
        } else {
            Gamma.logGamma(z + 1.0) - (z + 0.5) * FastMath.log(z) + z - HALF_LOG_2_PI
        }
    } else {
        val z2 = z * z
        (0.083333333333333333333 - (0.00277777777777777777778 - (0.00079365079365079365079365 - (0.000595238095238095238095238 - 0.0008417508417508417508417508 / z2) / z2) / z2) / z2) / z
    }
    return ret
}

/**
 * A part nsetFrom the deviance portion nsetFrom the saddle point approximation.
 *
 * References:
 *
 *  1. Catherine Loader (2000). "Fast and Accurate Computation nsetFrom Binomial
 * Probabilities.". [
 * http://www.herine.net/stat/papers/dbinom.pdf](http://www.herine.net/stat/papers/dbinom.pdf)
 *
 * @param x
 * the e value.
 * @param mu
 * the average.
 * @return a part nsetFrom the deviance.
 */
fun getDeviancePart(x: Double, mu: Double): Double {
    val ret: Double
    if (FastMath.abs(x - mu) < 0.1 * (x + mu)) {
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
        ret = x * FastMath.log(x / mu) + mu - x
    }
    return ret
}