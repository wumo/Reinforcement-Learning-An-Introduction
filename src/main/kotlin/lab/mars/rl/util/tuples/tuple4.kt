package lab.mars.rl.util.tuples

data class tuple4<A, B, C, D>(var _1: A, var _2: B, var _3: C, var _4: D) {
    override fun toString(): String {
        return "($_1,$_2,$_3,$_4)"
    }
}