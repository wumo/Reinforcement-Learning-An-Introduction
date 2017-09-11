package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-11.
 * </p>
 *
 * @author wumo
 */
fun IntArray.slice(start: Int, end: Int): IntSlice {
    return IntSlice(this, start, end - start + 1)
}

class IntSlice(private val array: IntArray, private val offset: Int, size: Int, cap: Int) {
    init {
        require(
                offset in 0..array.lastIndex &&
                size in 0..array.size - offset &&
                cap in size..array.size - offset)
    }

    var size = size
        private set
    var cap = cap
        private set
    var lastIndex = size - 1
        private set

    constructor(array: IntArray, offset: Int, size: Int) : this(array, offset, size, size)

    constructor(cap: Int = 2) : this(IntArray(cap), 0, cap)

    operator fun get(idx: Int): Int {
        require(idx in 0..lastIndex)
        return array[idx + offset]
    }

    operator fun get(start: Int, end: Int): IntSlice {
        return IntSlice(array, offset + start, size - (end - start))
    }

    operator fun set(idx: Int, s: Int) {
        array[idx] = s
    }

    operator fun set(start: Int, end: Int, s: Int) {

    }

    fun remove(start: Int, end: Int) {

    }

    fun toIntArray(): IntArray {
        TODO("not implemented")
    }

    fun append(size: Int, i: Int) {
        TODO("not implemented")
    }
}