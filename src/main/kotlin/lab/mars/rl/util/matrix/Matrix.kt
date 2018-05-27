@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util.matrix

operator fun Double.times(m: MatrixSpec) = m * this

operator fun Double.plus(m: MatrixSpec): Double {
  require(m.numRow == 1 && m.numCol == 1)
  return this + m[0, 0]
}

operator fun Double.minus(m: MatrixSpec): Double {
  require(m.numRow == 1 && m.numCol == 1)
  return this - m[0, 0]
}

inline fun <T> Σ(set: Iterable<T>, evaluate: (T) -> MatrixSpec): MatrixSpec {
  val iter = set.iterator()
  val first = iter.next()
  val sum = evaluate(first)
  while (iter.hasNext()) {
    val next = iter.next()
    sum += evaluate(next)
  }
  return sum
}

sealed class MatrixSpec(val numRow: Int, val numCol: Int) {
  val size = numRow * numCol
  operator fun get(a: Int) = get(a, 0)
  abstract operator fun get(a: Int, b: Int): Double
  operator fun set(a: Int, value: Double) = set(a, 0, value)
  abstract operator fun set(a: Int, b: Int, value: Double)
  
  operator fun minus(scalar: Double): Double {
    require(numRow == 1 && numCol == 1)
    return this[0, 0] - scalar
  }
  
  operator fun plus(scalar: Double): Double {
    require(numRow == 1 && numCol == 1)
    return this[0, 0] + scalar
  }
  
  operator fun times(scalar: Double): MatrixSpec {
    if (scalar == 0.0) return make(numRow, numCol)
    val m = copy()
    m *= scalar
    return m
  }
  
  inline operator fun timesAssign(scalar: Double) = piecewiseOp(this) { a, b, v ->
    this[a, b] = v * scalar
  }
  
  operator fun div(scalar: Double): MatrixSpec {
    val m = copy()
    m /= scalar
    return m
  }
  
  inline operator fun divAssign(scalar: Double) = piecewiseOp(this) { a, b, v ->
    this[a, b] = v / scalar
  }
  
  val toScalar: Double
    get() {
      require(numRow == 1 && numCol == 1)
      return this[0, 0]
    }
  
  val T: MatrixSpec
    get() {
      val m = make(numCol, numRow)
      piecewiseOp(this) { a, b, v ->
        m[b, a] = v
      }
      return m
    }
  
  operator fun plus(m: MatrixSpec): MatrixSpec {
    val result = copy()
    result += m
    return result
  }
  
  operator fun minus(m: MatrixSpec): MatrixSpec {
    require(numRow == m.numRow && numCol == m.numCol)
    val result = copy()
    result -= m
    return result
  }
  
  operator fun plusAssign(m: MatrixSpec) = piecewiseOp(m) { a, b, v ->
    this[a, b] += v
  }
  
  operator fun minusAssign(m: MatrixSpec) = piecewiseOp(m) { a, b, v ->
    this[a, b] -= v
  }
  
  abstract infix fun `T*`(m: MatrixSpec): MatrixSpec
  
  abstract operator fun times(m: MatrixSpec): MatrixSpec
  
  fun copy(): MatrixSpec {
    val result = make(numRow, numCol)
    result `=` this
    return result
  }
  
  abstract fun make(numRow: Int, numCol: Int): MatrixSpec
  
  infix fun o(m: MatrixSpec): MatrixSpec {
    val result = copy()
    result `o=` m
    return result
  }
  
  infix fun `o=`(m: MatrixSpec) = piecewiseOp(m) { a, b, v ->
    this[a, b] *= v
  }
  
  abstract fun zero()
  
  open infix fun `=`(m: MatrixSpec) = piecewiseOp(m) { a, b, v ->
    this[a, b] = v
  }
  
  inline fun piecewiseOp(m: MatrixSpec, op: (Int, Int, v: Double) -> Unit) {
    require(numRow == m.numRow && numCol == m.numCol)
    when (m) {
      is Matrix -> {
        for (a in 0 until m.numRow)
          for (b in 0 until m.numCol)
            op(a, b, m[a, b])
      }
      is SparseMatrix -> {
        for ((a, col) in m.rows)
          for ((b, v) in col)
            op(a, b, v)
      }
    }
  }
}

class Matrix(numRow: Int, numCol: Int = numRow) : MatrixSpec(numRow, numCol) {
  companion object {
    inline fun identity(d: Int): Matrix {
      val m = Matrix(d)
      for (i in 0 until d)
        m[i, i] = 1.0
      return m
    }
    
    inline fun one(d: Int) = column(d, 1.0)
    
    inline fun column(d: Int) = Matrix(d, 1)
    
    inline fun column(d: Int, value: Double) = Matrix.column(d) { value }
    
    inline fun column(value: DoubleArray): Matrix {
      val m = column(value.size)
      for (a in 0 until m.numRow)
        m[a, 0] = value[a]
      return m
    }
    
    inline fun column(d: Int, init: (Int) -> Double): Matrix {
      val m = column(d)
      for (a in 0 until m.numRow)
        m[a, 0] = init(a)
      return m
    }
  }
  
  private val raw = DoubleArray(size)
  
  inline fun rawIdx(a: Int, b: Int) = a * numCol + b
  override operator fun get(a: Int, b: Int) = raw[rawIdx(a, b)]
  override operator fun set(a: Int, b: Int, value: Double) {
    raw[rawIdx(a, b)] = value
  }
  
  override fun make(numRow: Int, numCol: Int) = Matrix(numRow, numCol)
  
  override fun zero() {
    for (i in 0..raw.lastIndex)
      raw[i] = 0.0
  }
  
  override operator fun times(m: MatrixSpec): MatrixSpec {
    require(numCol == m.numRow)
    return when (m) {
      is Matrix -> {
        val result = Matrix(numRow, m.numCol)
        for (a in 0 until numRow)
          for (b in 0 until m.numCol) {
            var sum = 0.0
            for (k in 0 until numCol)
              sum += this[a, k] * m[k, b]
            result[a, b] = sum
          }
        result
      }
      is SparseMatrix -> {
        val result = SparseMatrix(numRow, m.numCol)
        for (a in 0 until numRow)
          for ((colIdx, col) in m.cols) {
            var sum = 0.0
            for ((rowIdx, v) in col)
              sum += this[a, rowIdx] * v
            result[a, colIdx] = sum
          }
        result
      }
    }
  }
  
  override fun `T*`(m: MatrixSpec): MatrixSpec {
    require(numRow == m.numRow)
    return when (m) {
      is Matrix -> {
        val result = Matrix(numCol, m.numCol)
        for (a in 0 until numCol)
          for (b in 0 until m.numCol) {
            var sum = 0.0
            for (k in 0 until numRow)
              sum += this[k, a] * m[k, b]
            result[a, b] = sum
          }
        result
      }
      is SparseMatrix -> {
        val result = SparseMatrix(numCol, m.numCol)
        for (a in 0 until numCol)
          for ((b, col) in m.cols) {
            var sum = 0.0
            for ((k, v) in col)
              sum += this[k, a] * v
            result[a, b] = sum
          }
        result
      }
    }
  }
}

class SparseMatrix(numRow: Int, numCol: Int = numRow) : MatrixSpec(numRow, numCol) {
  val rows = HashMap<Int, HashMap<Int, Double>>()
  val cols = HashMap<Int, HashMap<Int, Double>>()
  
  override fun get(a: Int, b: Int): Double {
    val col = rows[a] ?: return 0.0
    return col[b] ?: 0.0
  }
  
  override fun set(a: Int, b: Int, value: Double) {
//    if (value == 0.0) {
//      val col = rows[a] ?: return
//      col.remove(b)
//      if (col.isEmpty()) rows.remove(a)
//      val row = cols[b] ?: return
//      row.remove(a)
//      if (row.isEmpty()) cols.remove(b)
//    } else {
    rows.compute(a) { _, col ->
      (col ?: HashMap()).apply {
        this[b] = value
      }
    }
    cols.compute(b) { _, row ->
      (row ?: HashMap()).apply {
        this[a] = value
      }
    }
//    }
  }
  
  override fun make(numRow: Int, numCol: Int) = SparseMatrix(numRow, numCol)
  
  override fun zero() {
    rows.clear()
    cols.clear()
  }
  
  override fun times(m: MatrixSpec): MatrixSpec {
    require(numCol == m.numRow)
    return when (m) {
      is Matrix -> {
        val result = SparseMatrix(numRow, m.numCol)
        for ((a, row) in rows) {
          for (b in 0 until m.numCol) {
            var sum = 0.0
            for ((k, v) in row)
              sum += v * m[k, b]
            result[a, b] = sum
          }
        }
        result
      }
      is SparseMatrix -> {
        val result = SparseMatrix(numRow, m.numCol)
        for ((a, row) in rows) {
          for ((b, col) in m.cols) {
            var sum = 0.0
            var v1 = row
            var v2 = col
            if (v1.size > v2.size) {
              val tmp = v1
              v1 = v2
              v2 = tmp
            }
            for ((k, v) in v1)
              sum += v * (v2[k] ?: 0.0)
            result[a, b] = sum
          }
        }
        result
      }
    }
  }
  
  override fun `T*`(m: MatrixSpec): MatrixSpec {
    require(numRow == m.numRow)
    return when (m) {
      is Matrix -> {
        val result = SparseMatrix(numCol, m.numCol)
        for ((a, col) in cols) {
          for (b in 0 until m.numCol) {
            var sum = 0.0
            for ((k, v) in col)
              sum += v * m[k, b]
            result[a, b] = sum
          }
        }
        result
      }
      is SparseMatrix -> {
        val result = SparseMatrix(numCol, m.numCol)
        for ((a, row) in cols) {
          for ((b, col) in m.cols) {
            var sum = 0.0
            var v1 = row
            var v2 = col
            if (v1.size > v2.size) {
              val tmp = v1
              v1 = v2
              v2 = tmp
            }
            for ((k, v) in v1)
              sum += v * (v2[k] ?: 0.0)
            result[a, b] = sum
          }
        }
        result
      }
    }
  }
}