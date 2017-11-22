package lab.mars.rl.model

import lab.mars.rl.util.matrix.Matrix

interface ActionValueApproxFunction {
    val w: Matrix
    /**
     * @return v(S,w)
     */
    operator fun invoke(s: IndexedState, a: IndexedAction): Double

    /**
     * @param s current state
     * @param delta `α*[Gt-v(S,w)]`, do not multiply `∇`
     */
    fun update(s: IndexedState, a: IndexedAction, delta: Double)

    fun `▽`(s: IndexedState, a: IndexedAction): Matrix
}