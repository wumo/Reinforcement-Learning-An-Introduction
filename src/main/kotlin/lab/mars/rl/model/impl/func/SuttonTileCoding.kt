package lab.mars.rl.model.impl.func

import lab.mars.rl.model.State
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.buf.newIntBuf
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.tuples.tuple2
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.ceil

class SuttonTileCoding(size: Int, val numTilings: Int, val tileConv: (State) -> tuple2<DoubleArray, IntArray>) : Feature {
    override val numOfComponents = numTilings * size
    override fun invoke(s: State): Matrix {
        val (floats, ints) = tileConv(s)
        val activeTiles = tiles(floats, ints)
        val x = Matrix.column(numOfComponents) { 0.0 }
        for (activeTile in activeTiles)
            x[activeTile] = 1.0
        return x
    }

    private val data = HashMap<Index, Int>(ceil(size / 0.75).toInt())

    private fun tiles(floats: DoubleArray, ints: IntArray): IntArray {
        val qfloats = IntArray(floats.size) { FastMath.floor(floats[it] * numTilings).toInt() }
        val result = IntArray(floats.size)
        for (tiling in 0 until numTilings) {
            val tilingX2 = tiling * 2
            val coords = newIntBuf(1 + floats.size + ints.size)
            coords.append(tiling)
            var b = tiling
            for (q in qfloats) {
                coords.append((q + b) / numTilings)
                b += tilingX2
            }
            coords.append(ints)
            result[tiling] = data[coords] ?: data.size.apply { data[coords] = data.size }
        }
        return result
    }

    override fun alpha(alpha: Double, s: State) = alpha / numTilings
}