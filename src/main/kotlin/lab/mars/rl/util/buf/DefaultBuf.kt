@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.util.buf

import lab.mars.rl.util.dimension.NULL_obj

/**
 * <p>
 * Created on 2017-09-28.
 * </p>
 *
 * @author wumo
 */
inline fun <reified T : Any> Array<T>.buf(start: Int = 0, end: Int = this.lastIndex) = DefaultBuf.reuse<T>(this as Array<Any>, start, end)

inline fun <T : Any> newBuf(cap: Int = 8, size: Int = 0) = DefaultBuf.new<T>(cap, size)
inline fun <T : Any> zeroBuf(num: Int) = DefaultBuf.zero<T>(num)
inline fun <T : Any> bufOf(vararg s: T) = DefaultBuf<T>(s as Array<Any>, 0, s.size)
inline fun <T : Any> reuseBuf(array: Array<Any>,
                              start: Int = 0,
                              end: Int = array.lastIndex,
                              cap: Int = array.size) = DefaultBuf.reuse<T>(array, start, end, cap)

open class DefaultBuf<T : Any>(private var ring: Array<Any>, private var offset: Int, size: Int, cap: Int = size) :
        MutableBuf<T> {
    companion object {
        val MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
        /**
         * @param s 用枚举的参数构成初始的[DefaultIntBuf]
         */
        inline fun <T : Any> of(vararg s: T) = DefaultBuf<T>(s as Array<Any>, 0, s.size)

        /**
         * @param num 初始化[num]长度、初值为0的[DefaultIntBuf]
         */
        inline fun <T : Any> zero(num: Int) = new<T>(num, num)

        /**
         *仅这里的[start]和[end]可以不满足[end]>=[start]的约束，表示环型数组的部分，其他方法则必须满足[end]>=[start]的约束
         */
        fun <T : Any> reuse(array: Array<Any>,
                            start: Int = 0,
                            end: Int = array.lastIndex,
                            cap: Int = array.size): DefaultBuf<T> {
            var size = end - start + 1
            if (size < 0) size += array.size
            return DefaultBuf(array, start, size, cap)
        }

        inline fun <T : Any> new(cap: Int = 8, size: Int = 0) = DefaultBuf<T>(Array(cap) { NULL_obj }, 0, size, cap)

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

    override operator fun get(idx: Int): T {
        require(idx in 0 until _size)
        return ring[index(idx)] as T
    }

    override operator fun get(start: Int, end: Int): DefaultBuf<T> {
        require(start in 0..end)
        require(end < _size)
        return DefaultBuf(ring, index(start), (end - start) + 1)
    }

    override operator fun set(idx: Int, s: T) {
        require(idx in 0 until _size)
        ring[index(idx)] = s
    }

    override operator fun set(start: Int, end: Int, s: T) {
        require(start in 0..end)
        require(end < _size)
        for (idx in start..end)
            ring[index(idx)] = s
    }

    override fun unfold(num: Int) {
        require(_size + num <= cap) { "$_size+$num >$cap" }
        _size += num
    }

    private tailrec fun ringCopy(src: Array<Any>, srcPos: Int, dest: Array<Any>, destPos: Int, length: Int) {
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

    override fun toTypedArray(): Array<T> {
        val result = Array(_size) { NULL_obj }
        ringCopy(ring, offset, result, 0, _size)
        return result as Array<T>
    }

    override fun copy() = reuse<T>(toTypedArray() as Array<Any>)

    override fun reuseBacked() = DefaultBuf<T>(ring, offset, size, cap)

    override fun ensure(minCap: Int) {
        if (_cap >= minCap) return
        _cap += _cap
        if (minCap > _cap)
            _cap = minCap
        if (_cap > MAX_ARRAY_SIZE)
            _cap = Int.MAX_VALUE
        if (_cap > ring.size) {
            val new_ring = Array(_cap) { NULL_obj }
            ringCopy(ring, offset, new_ring, 0, _size)
            ring = new_ring
            offset = 0
        }
    }

    override fun prepend(s: T) {
        ensure(_size + 1)
        val new_offset = index(-1 + ring.size)
        ring[new_offset] = s
        offset = new_offset
        _size++
    }

    override fun prepend(num: Int, s: T) {
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(-num + a + ring.size)] = s
        offset = index(-num + ring.size)
        _size += num
    }

    override fun prepend(another: Buf<T>) {
        val num = another.size
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(-num + a + ring.size)] = another[a]
        offset = index(-num + ring.size)
        _size += num
    }

    override fun append(s: T) {
        ensure(_size + 1)
        ring[index(_size)] = s
        _size++
    }

    override fun append(num: Int, s: T) {
        ensure(_size + num)
        for (a in 0 until num)
            ring[index(_size + a)] = s
        _size += num
    }

    override fun append(another: Buf<T>) {
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