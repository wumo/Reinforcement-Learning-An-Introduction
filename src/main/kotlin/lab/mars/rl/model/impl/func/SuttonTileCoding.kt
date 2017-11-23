package lab.mars.rl.model.impl.func

import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.buf.newIntBuf
import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.tuples.tuple2
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.ceil

val MAXIMUM_CAPACITY = 1 shl 30

class SuttonTileCoding(numTilesOfEachTiling: Int, _numTilings: Int) : Feature<tuple2<DoubleArray, IntArray>> {
    val numTilings = tableSizeFor(_numTilings)
    override val numOfComponents = numTilings * (numTilesOfEachTiling + 1)
    override fun invoke(s: tuple2<DoubleArray, IntArray>): Matrix {
        val (floats, ints) = s
        val activeTiles = tiles(floats, ints)
        val x = Matrix.column(numOfComponents) { 0.0 }
        for (activeTile in activeTiles)
            x[activeTile] = 1.0
        return x
    }

    val data = HashMap<Index, Int>(ceil(numTilesOfEachTiling / 0.75).toInt())

    private fun tiles(floats: DoubleArray, ints: IntArray): IntArray {
        val qfloats = IntArray(floats.size) { FastMath.floor(floats[it] * numTilings).toInt() }
        val result = IntArray(numTilings)
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

    /** Returns a power of two size for the given target capacity.*/
    fun tableSizeFor(cap: Int): Int {
        var n = cap - 1
        n = n or n.ushr(1)
        n = n or n.ushr(2)
        n = n or n.ushr(4)
        n = n or n.ushr(8)
        n = n or n.ushr(16)
        return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
    }
}