package lab.mars.rl.model.impl.func

import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.util.matrix.Matrix
import org.apache.commons.math3.util.FastMath.*

interface Feature<in E> {
    operator fun invoke(s: E): Matrix
    val numOfComponents: Int
}

operator fun DoubleArray.times(elements: DoubleArray): Double {
    var result = 0.0
    for (i in 0..lastIndex)
        result += this[i] * elements[i]
    return result
}

class SimplePolynomial(override val numOfComponents: Int) : Feature<Double> {
    override fun invoke(s: Double) = Matrix.column(numOfComponents) {
        pow(s, it)
    }
}

class SimpleFourier(override val numOfComponents: Int) : Feature<Double> {
    override fun invoke(s: Double) = Matrix.column(numOfComponents) {
        cos(it * PI * s)
    }
}

class LinearFunc<in E>(val x: Feature<E>) : ApproximateFunction<E> {
    override fun `▽`(input: E) = x(input)

    override val w = Matrix.column(x.numOfComponents)

    override fun invoke(input: E) = (w.T * x(input)).asScalar()
}