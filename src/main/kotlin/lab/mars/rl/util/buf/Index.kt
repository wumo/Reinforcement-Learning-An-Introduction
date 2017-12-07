@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util.buf

abstract class Index: Iterable<Int> {
  val isEmpty: Boolean
    get() = size == 0
  val isNotEmpty: Boolean
    get() = !isEmpty
  abstract val size: Int
  val lastIndex: Int
    get() = size - 1
  
  /**
   * get value at the specific [idx]
   */
  abstract operator fun get(idx: Int): Int
  
  open fun forEach(start: Int = 0, end: Int = lastIndex, block: (Int, Int) -> Unit) {
    for (i in start..end)
      block(i, get(i))
  }
  
  override fun iterator() = object: Iterator<Int> {
    var a = 0
    override fun hasNext() = a < size
    
    override fun next() = get(a++)
  }
  
  override fun toString(): String {
    val sb = StringBuilder()
    sb.append("[")
    if (isNotEmpty) {
      for (idx in 0 until lastIndex)
        sb.append(get(idx)).append(", ")
      sb.append(get(lastIndex))
    }
    sb.append("]")
    return sb.toString()
  }
  
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Index) return false
    if (size != other.size) return false
    for (a in 0..lastIndex)
      if (get(a) != other[a]) return false
    return true
  }
  
  override fun hashCode(): Int {
    if (isEmpty) return 0
    var result = get(0)
    for (a in 1..lastIndex)
      result = 31 * result + get(a)
    return result
  }
  
  operator fun component1() = this[0]
  operator fun component2() = this[1]
  operator fun component3() = this[2]
  operator fun component4() = this[3]
  operator fun component5() = this[4]
}

class MultiIndex(internal val indices: Array<Index>): Index() {
  override fun forEach(start: Int, end: Int, block: (Int, Int) -> Unit) {
    var start_index = -1
    var start_index_offset = 0
    var end_index = -1
    var end_index_offset = 0
    
    locate(start) { idx, dim ->
      start_index = idx; start_index_offset = dim
    }
    locate(start_index_offset + (end - start), start_index) { idx, dim ->
      end_index = idx; end_index_offset = dim
    }
    if (start_index == end_index) //在同一块
      iterate(start, 0, start_index, start_index_offset, end_index_offset) { a, b ->
        block(a, b)
      }
    else {
      var count = 0
      count = iterate(start, count, start_index, start_index_offset) { a, b ->
        block(a, b)
      }
      for (idx in start_index + 1 until end_index)
        count = iterate(start, count, idx, 0) { a, b ->
          block(a, b)
        }
      iterate(start, count, end_index, 0, end_index_offset) { a, b ->
        block(a, b)
      }
    }
  }
  
  private inline fun locate(dim: Int, start_Idx: Int = 0, block: (Int, Int) -> Unit) {
    var _dim = dim
    for (i in start_Idx until indices.size) {
      val index = indices[i]
      if (_dim < index.size) {
        block(i, _dim)
        return
      } else _dim -= index.size
    }
    throw IndexOutOfBoundsException()
  }
  
  private inline fun iterate(start: Int, count: Int, idx: Int, start_offset: Int, end_offset: Int = Int.MAX_VALUE, block: (Int, Int) -> Unit): Int {
    var _count = count
    val index = indices[idx]
    for (i in start_offset..minOf(end_offset, index.size - 1))
      block(start + _count++, index[i])
    return _count
  }
  
  override val size: Int = indices.sumBy { it.size }
  
  override fun iterator() = object: Iterator<Int> {
    var a = 0
    var b = 0
    override fun hasNext() = a < indices.size && b < indices[a].size
    
    override fun next(): Int {
      val result = indices[a][b]
      b++
      if (b >= indices[a].size) {
        a++
        b = 0
      }
      return result
    }
    
  }
  
  override fun get(idx: Int): Int {
    var _dim = idx
    for (index in indices)
      if (_dim < index.size) return index[_dim]
      else _dim -= index.size
    throw IndexOutOfBoundsException()
  }
}