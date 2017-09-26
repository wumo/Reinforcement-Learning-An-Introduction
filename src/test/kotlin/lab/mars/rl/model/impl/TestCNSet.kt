package lab.mars.rl.model.impl

import lab.mars.rl.util.MoreCompactNSet
import lab.mars.rl.util.MoreCompactNSet.Cell
import lab.mars.rl.util.MoreCompactNSet.SubTree
import lab.mars.rl.util.slice
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
        val set = MoreCompactNSet<Int>(data.slice())
        assert(set[0, 0], 1)
        assert(set[0, 1], 2)
        assert(set[0, 2, 0], 3)
        assert(set[0, 2, 1], 4)
        assert(set[1], 5)
        set.dfs(0){idx,value-> println("$idx=$value")}

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
    }
}