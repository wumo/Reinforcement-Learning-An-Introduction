package lab.mars.rl.model.impl.func

import lab.mars.rl.model.ApproximateFunction
import lab.mars.rl.util.matrix.Matrix
import org.apache.commons.math3.util.FastMath.*

abstract class Feature<E>(val conv: (Array<out Any>) -> E) {
  operator fun invoke(vararg args: Any): Matrix
      = _invoke(conv(args))
  
  abstract fun _invoke(s: E): Matrix
  abstract val numOfComponents: Int
}

operator fun DoubleArray.times(elements: DoubleArray): Double {
  var result = 0.0
  for (i in 0..lastIndex)
    result += this[i] * elements[i]
  return result
}

class SimplePolynomial(override val numOfComponents: Int, conv: (Array<out Any>) -> Double): Feature<Double>(conv) {
  override fun _invoke(s: Double) = Matrix.column(numOfComponents) {
    pow(s, it)
  }
}

class SimpleFourier(override val numOfComponents: Int, conv: (Array<out Any>) -> Double): Feature<Double>(conv) {
  override fun _invoke(s: Double) = Matrix.column(numOfComponents) {
    cos(it * PI * s)
  }
}

class LinearFunc<E>(val x: Feature<E>): ApproximateFunction<E>(x.conv) {
  override fun `_▽`(input: E) = x._invoke(input)
  
  override val w = Matrix.column(x.numOfComponents)
  
  override fun _invoke(input: E) = (w.T * x._invoke(input)).toScalar
}