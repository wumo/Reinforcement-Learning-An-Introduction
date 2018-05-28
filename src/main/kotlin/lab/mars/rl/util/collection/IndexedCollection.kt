@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package lab.mars.rl.util.collection

import lab.mars.rl.model.RandomIterable
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.Index
import lab.mars.rl.util.buf.MultiIndex
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.tuples.tuple2

interface IndexedCollection<E: Any>: RandomIterable<E>, Gettable<Index, E> {
  /**
   * make a collection with same shape (both dimension pair depth will be the same)
   */
  fun <T: Any> copycat(element_maker: (Index) -> T): IndexedCollection<T>
  
  fun copy() = copycat { get(it) }
  
  fun indices(): Iterator<Index>
  
  fun withIndices(): Iterator<tuple2<out Index, E>>
  
  override operator fun get(dim: Index): E
  operator fun get(vararg dim: Int): E = get(DefaultIntBuf.reuse(dim))
  operator fun get(vararg dim: Index): E = get(MultiIndex(dim as Array<Index>))
  
  /**
   * If the element at the [subset_dim] is a [IndexedCollection], then the result
   * will a subset at the position. And because it is a subset, the index prefix will be trimmed.
   * for example, [0,0,0] will be `[0,0]` afterStartup `invoke(0)`
   */
  operator fun invoke(subset_dim: Index): IndexedCollection<E>
  
  /**@see invoke */
  operator fun invoke(vararg subset_dim: Int): IndexedCollection<E> = invoke(DefaultIntBuf.reuse(subset_dim))
  
  /**@see invoke */
  operator fun invoke(vararg subset_dim: Index): IndexedCollection<E> = invoke(MultiIndex(subset_dim as Array<Index>))
  
  /**
   * return the element at [idx]. the order here is same with [iterator]
   */
  fun at(idx: Int): E {
    var i = 0
    for (element in this)
      if (i++ == idx) return element
    throw  NoSuchElementException()
  }
  
  /**
   * get one element with equal probability.
   */
  override fun rand() = at(Rand().nextInt(size))
  
  /**
   * get an element according to the probability distribution
   * @throws ClassCastException the element is not [Index] type
   * @throws IllegalArgumentException  if the [prob] is not a valid probability distribution
   * @throws NoSuchElementException if the set is empty
   */
  fun rand(prob: (E) -> Double): E {
    if (isEmpty()) throw NoSuchElementException()
    val p = Rand().nextDouble()
    var acc = 0.0
    for (element in this) {
      acc += prob(element)
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
   * if the set is not empty then execute [block]
   */
  fun ifAny(block: IndexedCollection<E>.(IndexedCollection<E>) -> Unit) {
    for (element in this) return block(this, this)
  }
  
  fun isEmpty(): Boolean {
    for (element in this) return false
    return true
  }
  
  override val size: Int
    get() {
      var count = 0
      for (element in this)
        count++
      return count
    }
}

inline fun <T: Any> IndexedCollection<T>.isNotEmpty() = !isEmpty()

interface ExtendableRAC<E: Any>: IndexedCollection<E> {
  operator fun set(subset_dim: Index, s: IndexedCollection<E>)
  operator fun set(vararg subset_dim: Int, s: IndexedCollection<E>) = set(DefaultIntBuf.reuse(subset_dim), s)
  operator fun set(vararg subset_dim: Index, s: IndexedCollection<E>) = set(MultiIndex(subset_dim as Array<Index>), s)
  
  fun <T: Any> raw_set(element_maker: (Index, E) -> T) {
    withIndices().forEach { (idx, value) ->
      val tmp = element_maker(idx, value)
      (tmp as? IndexedCollection<E>)?.apply {
        set(idx, this)
      } ?: set(idx, tmp as E)
    }
  }
}