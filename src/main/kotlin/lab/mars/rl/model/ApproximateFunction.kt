package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

abstract class ApproximateFunction<E>(var converter: (Array<out Any>) -> E) {
    abstract val w: Matrix

    operator fun invoke(vararg args: Any)
        = _invoke(converter(args))

    fun `▽`(vararg args: Any) = `_▽`(converter(args))

    abstract fun _invoke(input: E): Double
    abstract fun `_▽`(input: E): Matrix
}