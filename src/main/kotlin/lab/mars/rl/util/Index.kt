@file:Suppress("NOTHING_TO_INLINE")

package lab.mars.rl.util


interface Index {
    val size: Int
    val lastIndex: Int
        get() = size - 1

    /**
     * 获取指定维度[dim]上的数值
     */
    operator fun get(dim: Int): Int

    fun forEach(start: Int = 0, end: Int = lastIndex, block: (Int, Int) -> Unit) {
        for (i in start..end)
            block(i, get(i))
    }
}


class WrappedIndex(private val indices: Array<out Index>) : Index {
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

    companion object {
        inline fun of(indices: Array<out Index>) = WrappedIndex(indices)
    }

    override val size: Int = indices.sumBy { it.size }

    override fun get(dim: Int): Int {
        var _dim = dim
        for (index in indices)
            if (_dim < index.size) return index[dim]
            else _dim -= index.size
        throw IndexOutOfBoundsException()
    }
}