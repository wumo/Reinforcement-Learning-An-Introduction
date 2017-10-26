package lab.mars.rl.util

import lab.mars.rl.util.buf.DefaultBuf

class HashMapRAC<E : Any>() : RandomAccessCollection<E> {
    private val raw = hashMapOf<Index, E>()
    private val contigus = DefaultBuf.new<E>()

    override fun <T : Any> copycat(element_maker: (Index) -> T): RandomAccessCollection<T> {
        TODO()
    }

    override fun indices() = raw.keys.iterator()

    override fun withIndices(): Iterator<tuple2<out Index, E>> {
        val iter = raw.entries.iterator()
        return object : Iterator<tuple2<out Index, E>> {
            override fun hasNext() = iter.hasNext()

            override fun next(): tuple2<out Index, E> {
                val entry = iter.next()
                return tuple2(entry.key, entry.value)
            }
        }
    }

    override fun rand() = contigus[Rand().nextInt(contigus.size)]

    override fun get(dim: Index) = raw[dim]!!

    override fun invoke(subset_dim: Index): RandomAccessCollection<E> {
        TODO()
    }

    override fun set(dim: Index, s: E) {
        raw.put(dim, s) ?: contigus.append(s)
    }

    override fun iterator() = raw.values.iterator()
}