package lab.mars.rl.util.tuples

data class tuple5<A, B, C, D, E>(var _1: A, var _2: B, var _3: C, var _4: D, var _5: E) {
  override fun toString(): String {
    return "($_1,$_2,$_3,$_4,$_5)"
  }
}