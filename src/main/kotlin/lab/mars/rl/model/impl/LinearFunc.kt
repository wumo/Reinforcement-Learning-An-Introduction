package lab.mars.rl.model.impl

import lab.mars.rl.model.IndexedState
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.matrix.Matrix
import org.apache.commons.math3.util.FastMath.*

interface Feature {
    operator fun invoke(s: IndexedState): Matrix
    fun alpha(alpha: Double, s: IndexedState): Double = alpha
    val numOfComponents: Int
}

operator fun DoubleArray.times(elements: DoubleArray): Double {
    var result = 0.0
    for (i in 0..lastIndex)
        result += this[i] * elements[i]
    return result
}

class SimplePolynomial(override val numOfComponents: Int, val scale: Double) : Feature {
    override fun invoke(s: IndexedState) = Matrix.column(numOfComponents) {
        pow(s[0].toDouble() * scale, it)
    }
}

class SimpleFourier(override val numOfComponents: Int, val scale: Double) : Feature {
    override fun invoke(s: IndexedState) = Matrix.column(numOfComponents) {
        cos(it * PI * s[0].toDouble() * scale)
    }
}

class LinearFunc(val x: Feature) : ValueFunction {
    override fun `▽`(s: IndexedState) = x(s)

    override val w = Matrix.column(x.numOfComponents)

    override fun invoke(s: IndexedState) = (w.T * x(s)).asScalar()
}