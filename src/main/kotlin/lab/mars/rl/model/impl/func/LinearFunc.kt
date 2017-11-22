package lab.mars.rl.model.impl.func

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.mdp.IndexedState
import lab.mars.rl.util.matrix.Matrix
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
        pow((s as IndexedState)[0].toDouble() * scale, it)
    }
}

class SimpleFourier(override val numOfComponents: Int, val scale: Double) : Feature {
    override fun invoke(s: State) = Matrix.column(numOfComponents) {
        cos(it * PI * (s as IndexedState)[0].toDouble() * scale)
    }
}

class LinearFunc(val x: Feature) : ValueFunction {
    override fun `▽`(s: State) = x(s)

    override val w = Matrix.column(x.numOfComponents)

    override fun invoke(s: State) = (w.T * x(s)).asScalar()
}