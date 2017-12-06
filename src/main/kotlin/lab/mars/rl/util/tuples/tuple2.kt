package lab.mars.rl.util.tuples

data class tuple2<A, B>(var _1: A, var _2: B) {
  override fun toString(): String = "($_1,$_2)"
  
  operator fun invoke(a: A, b: B): tuple2<A, B> {
    _1 = a
    _2 = b
    return this
  }
}
