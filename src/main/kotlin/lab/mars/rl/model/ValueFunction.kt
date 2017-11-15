package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

interface ValueFunction {
    /**
     * @return v(S,w)
     */
    operator fun invoke(s: State): Double

    /**
     * @param s current state
     * @param delta `α*[Gt-v(S,w)]`, do not multiply gradient
     */
    fun update(s: State, delta: Double)

    fun gradient(s: State): Matrix
}