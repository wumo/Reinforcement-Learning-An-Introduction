package lab.mars.rl.util.tuples

data class tuple2<A, B>(var _1: A, var _2: B) {
    override fun toString(): String {
        return "($_1,$_2)"
    }
}
