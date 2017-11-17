package lab.mars.rl.model.impl

import lab.mars.rl.model.State
import lab.mars.rl.util.matrix.Matrix

class SimpleTileCoding(val numOfTilings: Int,
                       tilingSize: Int,
                       val tileWidth: Int,
                       val tilingOffset: Double,
                       val scale: (State) -> Double) : Feature {
    val tilingSize = tilingSize + 1
    override val numOfComponents: Int
        get() = numOfTilings * tilingSize

    override fun invoke(s: State) =
            Matrix.column(numOfComponents) {
                val tilingIdx = it / tilingSize
                val tileIdx = it % tilingSize
                val start = -tileWidth + tilingIdx * tilingOffset + tileIdx * tileWidth
                val s = scale(s)
                if (start <= s && s < start + tileWidth) 1.0 else 0.0
            }

    override fun alpha(alpha: Double, s: State) = alpha / numOfTilings
}