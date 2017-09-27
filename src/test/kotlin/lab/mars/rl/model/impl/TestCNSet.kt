package lab.mars.rl.model.impl

import lab.mars.rl.util.*
import lab.mars.rl.util.BFSMoreCompactNSet.Cell
import lab.mars.rl.util.BFSMoreCompactNSet.SubTree
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
private fun assert(expected: Any, actual: Any) = expected == actual

class TestCNSet {
    @Test
    fun `raw make`() {
        /*
        o
        │╲
        o     5
        │\╲
        1  2   o
              ╱  ╲
            3          4
         */
        val data = arrayOf(Cell(arrayOf(SubTree(2, 4), SubTree(3, 1)).slice(), 1), 2, Cell(arrayOf(SubTree(2, 3)).slice(), 3), 4, 5)
        val set = BFSMoreCompactNSet<Int>(data.slice())
        assert(set[0, 0], 1)
        assert(set[0, 1], 2)
        assert(set[0, 2, 0], 3)
        assert(set[0, 2, 1], 4)
        assert(set[1], 5)
        set.dfs(0) { idx, value -> println("$idx=$value") }

        var i = 0
        val values = arrayOf(1, 2, 3, 4, 5)
        for (value in set)
            assert(values[i++], value)
        set[0, 0] = 5
        set[0, 1] = 6
        set[0, 2, 0] = 7
        set[0, 2, 1] = 8
        set[1] = 9
        assert(set[0, 0], 5)
        assert(set[0, 1], 6)
        assert(set[0, 2, 0], 7)
        assert(set[0, 2, 1], 8)
        assert(set[1], 9)
        println(set.toString())
        for ((idx, value) in set.withIndices()) {
            println("$idx=$value")
        }
    }

    @Test
    fun `make using GeneralDimension 3`() {
        var i = 0
        val set = mcnsetFrom(3) { i++ }
        i = 0
        val slot = DefaultIntBuf.of(0)
        for ((idx, value) in set.withIndices()) {
            assert(idx.equals(slot))
            assert(value == i)
            i++
            slot[slot.lastIndex] = i
        }
        println(set)
    }

    @Test
    fun `make using GeneralDimension 2 x 3`() {
        var i = 0
        val set = mcnsetFrom(2 x 3) { i++ }
        println(set)
        i = 0
        val dim = intArrayOf(2, 3)
        val slot = DefaultIntBuf.of(0, 0)
        for ((idx, value) in set.withIndices()) {
            println("$idx=$value")
            assert(idx.equals(slot))
            assert(value == i)
            i++
            slot.increment(dim)
        }

    }

    @Test
    fun `make using GeneralDimension 2 x 3 x 4`() {
        var i = 0
        val set = mcnsetFrom(2 x 3 x 4) { i++ }
        println(set)
        i = 0
        val dim = intArrayOf(2, 3, 4)
        val slot = DefaultIntBuf.of(0, 0, 0)
        for ((idx, value) in set.withIndices()) {
            println("$idx=$value")
            assert(idx.equals(slot))
            assert(value == i)
            i++
            slot.increment(dim)
        }
    }

    @Test
    fun `make using GeneralDimension 2 x { it+1 }`() {
        var i = 0
        val set = mcnsetFrom(2 x { it[0] + 1 }) { i++ }
        println(set)
//        i = 0
//        val dim = intArrayOf(2, 3)
//        val slot = DefaultIntBuf.of(0, 0)
//        for ((idx, value) in set.withIndices()) {
//            println("$idx=$value")
//            assert(idx.equals(slot))
//            assert(value == i)
//            i++
//            slot.increment(dim)
//        }
    }
}