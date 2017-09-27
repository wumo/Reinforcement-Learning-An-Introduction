@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-11.
 * </p>
 *
 * @author wumo
 */
fun IntArray.buf(start: Int, end: Int): DefaultIntBuf = DefaultIntBuf.reuse(this, start, end)

interface IntBuf : Index {
    /** [end]>=[start] */
    operator fun get(start: Int, end: Int): IntBuf

    fun toIntArray(): IntArray
    fun copy(): IntBuf
}

interface MutableIntBuf : IntBuf {
    val cap: Int
    operator fun set(idx: Int, s: Int)

    /** [end]>=[start] */
    operator fun set(start: Int, end: Int, s: Int)

    fun ensure(minCap: Int)

    fun prepend(s: Int)
    fun prepend(num: Int, s: Int)
    fun prepend(another: IntBuf)

    fun append(s: Int)
    fun append(num: Int, s: Int)
    fun append(another: IntBuf)

    fun remove(range: IntRange) {
        remove(range.start, range.endInclusive)
    }

    /** [end]>=[start] */
    fun remove(start: Int, end: Int)

    fun remove(index: Int) = remove(index, index)
    fun removeFirst(num: Int) {
        if (num == 0) return
        remove(0, num - 1)
    }

    fun removeLast(num: Int) {
        if (num == 0) return
        remove(lastIndex - num + 1, lastIndex)
    }

    fun clear() {
        removeLast(size)
    }

    fun reuseBacked(): IntBuf
}

open class DefaultIntBuf(private var ring: IntArray, private var offset: Int, size: Int, cap: Int = size) :
        MutableIntBuf {
    companion object {
        val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
        /**
         * @param s 用枚举的参数构成初始的[DefaultIntBuf]
         */
        inline fun of(vararg s: Int) = DefaultIntBuf(s, 0, s.size)

        /**
         * @param num 初始化[num]长度、初值为0的[DefaultIntBuf]
         */
        inline fun zero(num: Int) = new(num, num)

        /**
         *仅这里的[start]和[end]可以不满足[end]>=[start]的约束，表示环型数组的部分，其他方法则必须满足[end]>=[start]的约束
         */
        fun reuse(array: IntArray, start: Int = 0, end: Int = array.lastIndex, cap: Int = array.size): DefaultIntBuf {
            var size = end - start + 1
            if (size < 0) size += array.size
            return DefaultIntBuf(array, start, size, cap)
        }

        inline fun new(cap: Int = 8, size: Int = 0) = DefaultIntBuf(IntArray(cap), 0, size, cap)

        inline fun from(s: Index) = reuse(IntArray(s.size) { s[it] })
    }

    init {
        require(
                offset in 0..ring.lastIndex &&
                size in 0..ring.size &&
                cap in size..ring.size)
    }

    private var _size = size
    private var _cap = cap

    override val size: Int
        get() = _size
    override val cap: Int
        get() = _cap


    private inline fun index(i: Int): Int = (offset + i) % ring.size

    override operator fun get(idx: Int): Int {
        require(idx in 0 until _size)
        return ring[index(idx)]
    }

    override operator fun get(start: Int, end: Int): DefaultIntBuf {
        require(start in 0..end)
        require(end < _size)
        return DefaultIntBuf(ring, index(start), _size - (end - start))
    }

    override operator fun set(idx: Int, s: Int) {
        require(idx in 0 until _size)
        ring[index(idx)] = s
    }

    override operator fun set(start: Int, end: Int, s: Int) {
        require(start in 0..end)
        require(end < _size)
        for (idx in start..end)
            ring[index(idx)] = s
    }

    private tailrec fun ringCopy(src: IntArray, srcPos: Int, dest: IntArray, destPos: Int, length: Int) {
        val c = minOf(src.size - srcPos, dest.size - destPos, length)
        System.arraycopy(src, srcPos, dest, destPos, c)
        if (c < length)
            ringCopy(src, (srcPos + c) % src.size, dest, (destPos + c) % dest.size, length - c)
    }

    override fun remove(start: Int, end: Int) {
        require(start in 0..end)
        require(end < _size)
        when {
            start == 0 -> {
                offset = index((end - start + 1))
            }
            end == lastIndex -> {
            }
            else -> {
                if (start > _size - end) //left > right, move right
                    ringCopy(ring, index(end + 1), ring, index(start), _size - 1 - end)
                else {
                    val c = start - offset
                    val new_offset = index(end - c + ring.size)
                    ringCopy(ring, offset, ring, new_offset, c)
                    offset = new_offset
                }
            }
        }
        _size -= (end - start + 1)
    }

    override fun toIntArray(): IntArray {
        val result = IntArray(_size)
        ringCopy(ring, offset, result, 0, _size)
        return result
    }

    override fun copy() = reuse(toIntArray())

    override fun reuseBacked() = DefaultIntBuf(ring, offset, size, cap)

    override fun ensure(minCap: Int) {
        if (_cap >= minCap) return
        _cap += _cap
        if (minCap > _cap)
            _cap = minCap
        if (_cap > MAX_ARRAY_SIZE)
            _cap = Int.MAX_VALUE
        if (_cap > ring.size) {
            val new_ring = IntArray(_cap)
            ringCopy(ring, offset, new_ring, 0, _size)
            ring = new_ring
            offset = 0
        }
    }

    override fun prepend(s: Int) {
        ensure(_size + 1)
        val new_offset = index(-1 + ring.size)
        ring[new_offset] = s
        offset = new_offset
        _size++
    }

    override fun prepend(num: Int, s: Int) {
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(-num + a + ring.size)] = s
        offset = index(-num + ring.size)
        _size += num
    }

    override fun prepend(another: IntBuf) {
        val num = another.size
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(-num + a + ring.size)] = another[a]
        offset = index(-num + ring.size)
        _size += num
    }

    override fun append(s: Int) {
        ensure(_size + 1)
        ring[index(_size)] = s
        _size++
    }

    override fun append(num: Int, s: Int) {
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(_size + a)] = s
        _size += num
    }

    override fun append(another: IntBuf) {
        val num = another.size
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(_size + a)] = another[a]
        _size += num
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("[")
        for (idx in 0 until lastIndex)
            sb.append(ring[index(idx)]).append(", ")
        sb.append(ring[index(lastIndex)])
        sb.append("]")
        return sb.toString()
    }

}

inline fun <reified T : Any> Array<T>.slice(start: Int = 0, end: Int = this.lastIndex) = Buf<T>(this as Array<Any>, start, end - start + 1)

class Buf<T : Any>(val backed: Array<Any>,
                   val start: Int = 0,
                   size: Int = backed.lastIndex,
                   val cap: Int = backed.size - start) {
    companion object {
        inline fun <reified T : Any> new(cap: Int = 8, size: Int = 0) = Buf<T>(Array(cap) { NULL_obj }, 0, size)
    }

    init {
        require(start in 0..backed.lastIndex)
        require(size in 0..backed.size)
    }

    var size = size
        private set

    var lastIndex = this.size - 1
        private set
        get() = this.size - 1

    operator fun get(idx: Int): T {
        require(idx in 0..lastIndex)
        return backed[start + idx] as T
    }

    operator fun set(idx: Int, s: T) {
        require(idx in 0..lastIndex)
        backed[start + idx] = s
    }

    operator fun plusAssign(s: T) {
        require(size + 1 <= cap)
        backed[start + size] = s
        size++
    }

    fun unfold(num: Int) {
        require(size + num <= cap)
        size += num
    }

    fun subBuf(start: Int, end: Int) = Buf<T>(backed, this.start + start, minOf(this.size, end - start + 1))

    inline fun forEach(start: Int = 0, end: Int = lastIndex, block: (T) -> Unit) {
        for (a in start..end)
            block(backed[this.start + a] as T)
    }

}