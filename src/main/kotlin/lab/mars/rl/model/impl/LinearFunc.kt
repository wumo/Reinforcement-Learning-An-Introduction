package lab.mars.rl.model.impl

import lab.mars.rl.model.State
import lab.mars.rl.model.ValueFunction
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.matrix.times
import org.apache.commons.math3.util.FastMath.*

interface Feature {
    operator fun invoke(s: State): Matrix
    fun alpha(alpha: Double, s: State): Double = alpha
    val numOfComponents: Int
}

operator fun DoubleArray.times(elements: DoubleArray): Double {
    var result = 0.0
    for (i in 0..lastIndex)
        result += this[i] * elements[i]
    return result
}

class SimplePolynomial(override val numOfComponents: Int, val scale: Double) : Feature {
    override fun invoke(s: State) = Matrix.column(numOfComponents) {
        pow(s[0].toDouble() * scale, it)
    }
}

class SimpleFourier(override val numOfComponents: Int, val scale: Double) : Feature {
    override fun invoke(s: State) = Matrix.column(numOfComponents) {
        cos(it * PI * s[0].toDouble() * scale)
    }
}

class LinearFunc(val x: Feature, val alpha: Double) : ValueFunction {
    override fun `∇`(s: State) = x(s)

    val w = Matrix.column(x.numOfComponents)

    override fun invoke(s: State) = (w.T * x(s)).asScalar()

    override fun update(s: State, delta: Double) {
        val alpha = x.alpha(alpha, s)
        w += alpha * delta * x(s)
    }
}