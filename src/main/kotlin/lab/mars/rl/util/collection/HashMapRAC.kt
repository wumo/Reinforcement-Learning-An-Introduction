package lab.mars.rl.util.collection

import lab.mars.rl.util.buf.DefaultBuf
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.tuples.tuple2

class HashMapRAC<E : Any>() : IndexedCollection<E> {
  private val raw = hashMapOf<Index, E>()
  private val contigus = DefaultBuf.new<E>()

  override fun <T : Any> copycat(element_maker: (Index) -> T): IndexedCollection<T> {
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

  override fun invoke(subset_dim: Index): IndexedCollection<E> {
    TODO()
  }

  override fun set(dim: Index, s: E) {
    raw.put(dim, s) ?: contigus.append(s)
  }

  override fun iterator() = raw.values.iterator()
}