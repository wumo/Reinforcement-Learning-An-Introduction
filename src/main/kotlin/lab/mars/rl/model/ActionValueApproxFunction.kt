package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

interface ActionValueApproxFunction {
    val w: Matrix
    /**
     * @return v(S,w)
     */
    operator fun invoke(s: State, a: Action): Double

    /**
     * @param s current state
     * @param delta `α*[Gt-v(S,w)]`, do not multiply `∇`
     */
    fun update(s: State, a: Action, delta: Double)

    fun `▽`(s: State, a: Action): Matrix
}