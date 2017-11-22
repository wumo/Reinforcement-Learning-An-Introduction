@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package lab.mars.rl.util.collection

import lab.mars.rl.util.buf.*
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.tuples.tuple2

interface IndexedCollection<E : Any> : Iterable<E> {
    /**
     * 构造一个与此集合相同形状的[IndexedCollection]（维度、树深度都相同）
     */
    fun <T : Any> copycat(element_maker: (Index) -> T): IndexedCollection<T>

    fun copy() = copycat { get(it) }

    fun indices(): Iterator<Index>

    fun withIndices(): Iterator<tuple2<out Index, E>>

    operator fun get(dim: Index): E
    operator fun get(vararg dim: Int): E = get(DefaultIntBuf.reuse(dim))
    operator fun get(vararg dim: Index): E = get(MultiIndex(dim as Array<Index>))

    /**
     * 对应位置元素为[IndexedCollection<E>]，则可以使用invoke操作符进行部分获取，
     * 由于获取的是子集，索引维度将要去掉前缀长度，如：原来通过`[0,0,0]`来索引，`invoke(0)`之后，则只能通过`[0,0]`来获取
     */
    operator fun invoke(subset_dim: Index): IndexedCollection<E>

    /**@see invoke */
    operator fun invoke(vararg subset_dim: Int): IndexedCollection<E> = invoke(DefaultIntBuf.reuse(subset_dim))

    /**@see invoke */
    operator fun invoke(vararg subset_dim: Index): IndexedCollection<E> = invoke(MultiIndex(subset_dim as Array<Index>))

    /**
     * 返回第[idx]个元素，这里的元素顺序与[iterator()]的顺序一致
     */
    fun at(idx: Int): E {
        var i = 0
        for (element in this)
            if (i++ == idx) return element
        throw  NoSuchElementException()
    }

    /**
     * 以等概率获取随意的一个元素
     */
    fun rand() = at(Rand().nextInt(size))

    /**
     * 如果此集合内的元素是[Index]类型，则可以提供概率分布[prob]，以此概
     * 率分布随机获取到元素
     * @throws ClassCastException 如果此集合内的元素不是[Index]类型
     * @throws IllegalArgumentException  如果提供的参数不是合法的概率分布
     * @throws NoSuchElementException 如果集合为空
     */
    fun rand(prob: IndexedCollection<Double>): E {
        if (isEmpty()) throw NoSuchElementException()
        val p = Rand().nextDouble()
        var acc = 0.0
        for (element in this) {
            acc += prob[element as Index]
            if (p <= acc)
                return element
        }
        throw IllegalArgumentException("random=$p, but accumulation=$acc")
    }

    operator fun set(dim: Index, s: E)
    operator fun set(vararg dim: Int, s: E) = set(DefaultIntBuf.reuse(dim), s)
    operator fun set(vararg dim: Index, s: E) = set(MultiIndex(dim as Array<Index>), s)

    fun set(element_maker: (Index, E) -> E) {
        withIndices().forEach { (idx, value) -> set(idx, element_maker(idx, value)) }
    }

    /**
     * 如果集合不为空，则执行[block]
     */
    fun ifAny(block: IndexedCollection<E>.(IndexedCollection<E>) -> Unit) {
        for (element in this) return block(this, this)
    }

    fun isEmpty(): Boolean {
        for (element in this) return false
        return true
    }

    val size: Int
        get() {
            var count = 0
            for (element in this)
                count++
            return count
        }
}

inline fun <T : Any> IndexedCollection<T>.isNotEmpty() = !isEmpty()

interface ExtendableRAC<E : Any> : IndexedCollection<E> {
    operator fun set(subset_dim: Index, s: IndexedCollection<E>)
    operator fun set(vararg subset_dim: Int, s: IndexedCollection<E>) = set(DefaultIntBuf.reuse(subset_dim), s)
    operator fun set(vararg subset_dim: Index, s: IndexedCollection<E>) = set(MultiIndex(subset_dim as Array<Index>), s)

    fun <T : Any> raw_set(element_maker: (Index, E) -> T) {
        withIndices().forEach { (idx, value) ->
            val tmp = element_maker(idx, value)
            (tmp as? IndexedCollection<E>)?.apply {
                set(idx, this)
            } ?: set(idx, tmp as E)
        }
    }
}