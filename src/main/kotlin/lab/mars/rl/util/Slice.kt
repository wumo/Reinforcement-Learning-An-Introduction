@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package lab.mars.rl.util

import java.util.*

/**
 * <p>
 * Created on 2017-09-11.
 * </p>
 *
 * @author wumo
 */
fun IntArray.slice(start: Int, end: Int): IntSlice {
    return IntSlice.use(this, start, end)
}

interface ReadOnlyIntSlice {
    val size: Int
    val cap: Int
    val lastIndex: Int

    operator fun get(idx: Int): Int

    operator fun get(start: Int, end: Int): ReadOnlyIntSlice

    fun toIntArray(): IntArray
    fun copyOf(): IntSlice
}

interface RWIntSlice : ReadOnlyIntSlice {
    operator fun set(idx: Int, s: Int)

    operator fun set(start: Int, end: Int, s: Int)

    fun remove(range: IntRange) {
        remove(range.start, range.endInclusive)
    }

    fun remove(start: Int, end: Int)
    fun remove(index: Int) = remove(index, index)
    fun removeLast(num: Int) = remove(lastIndex - num + 1, lastIndex)

    fun add(s: Int)

    fun add(num: Int, s: Int)
}

interface RWAIntSlice : RWIntSlice {
    fun ensure(minCap: Int)

    fun append(s: Int)

    fun append(num: Int, s: Int)
}

open class IntSlice constructor(private var array: IntArray, private var offset: Int, size: Int, cap: Int = size) :
        RWAIntSlice {
    companion object {
        val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
        /**
         * @param s 用枚举的参数构成初始的[IntSlice]
         */
        fun of(vararg s: Int) = IntSlice(s, 0, s.size)

        /**
         * @param num 初始化[num]长度、初值为0的[IntSlice]
         */
        inline fun zero(num: Int) = new(num, num)

        inline fun use(array: IntArray, start: Int, end: Int) = IntSlice(array, start, end - start + 1)

        inline fun use(array: IntArray) = use(array, 0, array.lastIndex)

        fun new(cap: Int = 2, size: Int = 0) = IntSlice(IntArray(cap), 0, size, cap)
    }

    init {
        require(
                offset in 0..array.lastIndex &&
                size in 0..array.size - offset &&
                cap in size..array.size - offset)
    }

    private var _size = size
    private var _cap = cap

    override val size: Int
        get() = _size
    override val cap: Int
        get() = _cap
    override val lastIndex: Int
        get() = _size - 1


    override operator fun get(idx: Int): Int {
        require(idx in 0 until _size)
        return array[idx + offset]
    }

    override operator fun get(start: Int, end: Int): IntSlice {
        require(start in 0..end)
        require(end < _size)
        return IntSlice(array, offset + start, _size - (end - start))
    }

    override operator fun set(idx: Int, s: Int) {
        require(idx in 0 until _size)
        array[idx + offset] = s
    }

    override operator fun set(start: Int, end: Int, s: Int) {
        require(start in 0..end)
        require(end < _size)
        for (idx in start..end)
            array[offset + idx] = s
    }

    override fun remove(start: Int, end: Int) {
        require(start in 0..end)
        require(end < _size)
        if (end < _size - 1) {
            val moved = _size - 1 - end
            System.arraycopy(array, offset + end + 1, array, offset + start, moved)
        }
        _size -= (end - start + 1)
    }

    override fun toIntArray(): IntArray = Arrays.copyOfRange(array, offset, offset + _size)

    override fun copyOf() = IntSlice.use(toIntArray())

    override fun add(s: Int) {
        require(_size < _cap)
        array[offset + _size] = s
        _size++
    }

    override fun add(num: Int, s: Int) {
        require(_size + num <= _cap)
        for (i in 0 until num)
            array[offset + _size + i] = s
        _size += num
    }

    override fun ensure(minCap: Int) {
        if (_cap >= minCap) return
        _cap += _cap
        if (minCap > _cap)
            _cap = minCap
        if (_cap > MAX_ARRAY_SIZE)
            _cap = Int.MAX_VALUE
        if (offset + _cap > array.size) {
            val tmp = IntArray(_cap)
            System.arraycopy(array, offset, tmp, 0, _size)
            array = tmp
            offset = 0
        }
    }

    final inline override fun append(s: Int) {
        ensure(size + 1)
        add(s)
    }

    final inline override fun append(num: Int, s: Int) {
        ensure(size + num)
        add(num, s)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[")
        for (idx in offset until lastIndex)
            sb.append(array[idx]).append(", ")
        sb.append(array[lastIndex])
        sb.append("]")
        return sb.toString()
    }


}