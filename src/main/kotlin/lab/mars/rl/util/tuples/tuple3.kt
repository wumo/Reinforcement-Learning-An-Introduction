package lab.mars.rl.util.tuples

data class tuple3<A, B, C>(var _1: A, var _2: B, var _3: C) {
    override fun toString(): String {
        return "($_1,$_2,$_3)"
    }
}
