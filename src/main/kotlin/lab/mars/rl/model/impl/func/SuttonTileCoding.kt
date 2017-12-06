package lab.mars.rl.model.impl.func

import lab.mars.rl.util.matrix.Matrix
import lab.mars.rl.util.tuples.tuple2
import org.apache.commons.math3.util.FastMath.*

val MAXIMUM_CAPACITY = 1 shl 30

class SuttonTileCoding(numTilesOfEachTiling: Int, _numTilings: Int, conv: (Array<out Any>) -> tuple2<DoubleArray, IntArray>): Feature<tuple2<DoubleArray, IntArray>>(conv) {
  val numTilings = tableSizeFor(_numTilings)
  override val numOfComponents = numTilings * (numTilesOfEachTiling + 1)
  override fun _invoke(s: tuple2<DoubleArray, IntArray>): Matrix {
    val (floats, ints) = s
    val activeTiles = tiles(floats, ints)
    val x = Matrix.column(numOfComponents)
    for (activeTile in activeTiles)
      x[activeTile] = 1.0
    return x
  }

  val data = HashMap<ArrayList<Double>, Int>(ceil(numOfComponents / 0.75).toInt())

  private fun tiles(floats: DoubleArray, ints: IntArray): IntArray {
    val qfloats = DoubleArray(floats.size) { floor(floats[it] * numTilings) }
    val result = IntArray(numTilings)
    for (tiling in 0 until numTilings) {
      val tilingX2 = tiling * 2
      val coords = ArrayList<Double>(1 + floats.size + ints.size)
      coords.add(tiling.toDouble())
      var b = tiling
      for (q in qfloats) {
        coords.add(floor(((q + b) / numTilings)))
        b += tilingX2
      }
      for (int in ints)
        coords.add(int.toDouble())
      result[tiling] = data.getOrPut(coords, { data.size })
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