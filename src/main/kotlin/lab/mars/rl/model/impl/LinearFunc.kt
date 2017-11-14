package lab.mars.rl.model.impl

import lab.mars.rl.model.State
import lab.mars.rl.model.ValueFunction
import org.apache.commons.math3.util.FastMath.*

interface featureFunc {
    operator fun invoke(s: State): DoubleArray
    fun alpha(alpha: Double, s: State): Double = alpha
    val featureNum: Int
}

operator fun DoubleArray.times(elements: DoubleArray): Double {
    var result = 0.0
    for (i in 0..lastIndex)
        result += this[i] * elements[i]
    return result
}

class SimplePolynomial(override val featureNum: Int, val scale: Double) : featureFunc {
    override fun invoke(s: State) = DoubleArray(featureNum) {
        pow(s[0].toDouble() * scale, it)
    }
}

class SimpleFourier(override val featureNum: Int, val scale: Double) : featureFunc {
    override fun invoke(s: State) = DoubleArray(featureNum) {
        cos(it * PI * s[0].toDouble() * scale)
    }
}

class LinearFunc(val x: featureFunc, val alpha: Double) : ValueFunction {
    val w = DoubleArray(x.featureNum) { 0.0 }

    override fun get(s: State) = w * x(s)

    override fun update(s: State, delta: Double) {
        val _x = x(s)
        val alpha = x.alpha(alpha, s)
        for (i in 0..w.lastIndex)
            w[i] += alpha * delta * _x[i]
    }
}