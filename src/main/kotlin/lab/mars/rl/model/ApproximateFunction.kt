package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

interface ApproximateFunction<in E> {
    val w: Matrix

    operator fun invoke(input: E): Double

    fun `▽`(input: E): Matrix
}