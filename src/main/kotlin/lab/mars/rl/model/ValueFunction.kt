package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

interface ValueFunction {
    val w: Matrix
    /**
     * @return v(S,w)
     */
    operator fun invoke(s: IndexedState): Double

    fun `▽`(s: IndexedState): Matrix
}