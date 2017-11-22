package lab.mars.rl.util.tile

import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.Index
import org.apache.commons.math3.util.FastMath.floor

class IHT(size: Int) {
    val data = HashMap<Index, Int>(size)

    fun tiles(numtilings: Int, vararg floats: Double): IntArray {
        val qfloats = IntArray(floats.size) { floor(floats[it] * numtilings).toInt() }
        val result = IntArray(floats.size)
        for (tiling in 0 until numtilings) {
            val tilingX2 = tiling * 2
            val coords = DefaultIntBuf.new(floats.size + 1)
            coords.append(0)
            var b = tiling
            for (q in qfloats) {
                coords.append((q + b) / numtilings)
                b += tilingX2
            }
            result[tiling] = data[coords] ?: data.size.apply { data[coords] = data.size }
        }
        return result
    }
}