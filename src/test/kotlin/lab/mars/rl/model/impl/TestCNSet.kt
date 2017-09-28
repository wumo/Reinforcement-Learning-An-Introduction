package lab.mars.rl.model.impl

import lab.mars.rl.util.*
import lab.mars.rl.util.BFSMoreCompactNSet.Cell
import lab.mars.rl.util.BFSMoreCompactNSet.SubTree
import lab.mars.rl.util.Bufkt.DefaultIntBuf
import lab.mars.rl.util.Bufkt.IntBuf
import lab.mars.rl.util.Bufkt.buf
import org.junit.Assert
import org.junit.Test
import java.util.*

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
        val data = arrayOf(Cell(arrayOf(SubTree(2, 4), SubTree(3, 1)).buf(), 1), 2, Cell(arrayOf(SubTree(2, 3)).buf(), 3), 4, 5)
        val set = BFSMoreCompactNSet<Int>(data.buf())
        val slots = arrayOf(
                DefaultIntBuf.of(0, 0),
                DefaultIntBuf.of(0, 1),
                DefaultIntBuf.of(0, 2, 0),
                DefaultIntBuf.of(0, 2, 1),
                DefaultIntBuf.of(1)
        )
        val values = arrayOf(
                1, 2, 3, 4, 5
        )
        assertEquals(PlainSet(slots, values), set)
//        set.dfs(0) { idx, value -> println("$idx=$value") }
    }

    @Test
    fun `make using GeneralDimension 3`() {
        var i = 0
        val set = mcnsetFrom(3) { i++ }
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0),
                        DefaultIntBuf.of(1),
                        DefaultIntBuf.of(2)
                ),
                values = arrayOf(
                        0, 1, 2
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using GeneralDimension 2 x 3`() {
        var i = 0
        val set = mcnsetFrom(2 x 3) { i++ }
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0, 0),
                        DefaultIntBuf.of(0, 1),
                        DefaultIntBuf.of(0, 2),
                        DefaultIntBuf.of(1, 0),
                        DefaultIntBuf.of(1, 1),
                        DefaultIntBuf.of(1, 2)
                ),
                values = arrayOf(
                        0, 1, 2, 3, 4, 5
                )
        )

        assertEquals(expected, set)

    }

    @Test
    fun `make using GeneralDimension 2 x 3 x 4`() {
        var i = 0
        val set = mcnsetFrom(2 x 3 x 4) { i++ }
        i = 0
        val slotList = arrayListOf<IntBuf>()
        val valuesList = arrayListOf<Int>()
        for (a in 0 until 2)
            for (b in 0 until 3)
                for (c in 0 until 4) {
                    slotList.add(DefaultIntBuf.of(a, b, c))
                    valuesList.add(i++)
                }

        val expected = PlainSet(
                slotList.toTypedArray(),
                valuesList.toTypedArray()
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using GeneralDimension 2 x { it+1 }`() {
        var i = 0
        val set = mcnsetFrom(2 x { it[0] + 1 }) { i++ }

        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0, 0),
                        DefaultIntBuf.of(1, 0),
                        DefaultIntBuf.of(1, 1)
                ),
                values = arrayOf(
                        0, 1, 2
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using Enumerated 0(!3) `() {
        var i = 0
        val set = mcnsetFrom(0(!3)) { i++ }
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0),
                        DefaultIntBuf.of(1),
                        DefaultIntBuf.of(2)
                ),
                values = arrayOf(
                        0, 1, 2
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using Enumerated 0(!3,!3) `() {
        var i = 0
        val set = mcnsetFrom(0(!3, !3)) { i++ }
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0),
                        DefaultIntBuf.of(1),
                        DefaultIntBuf.of(2),
                        DefaultIntBuf.of(3),
                        DefaultIntBuf.of(4),
                        DefaultIntBuf.of(5)
                ),
                values = arrayOf(
                        0, 1, 2, 3, 4, 5
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using GeneralDimension 0(!3, !3, 2 x 3) `() {
        var i = 0
        val set = mcnsetFrom(0(!3, !3, 2 x 3)) { i++ }
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0),
                        DefaultIntBuf.of(1),
                        DefaultIntBuf.of(2),
                        DefaultIntBuf.of(3),
                        DefaultIntBuf.of(4),
                        DefaultIntBuf.of(5),
                        DefaultIntBuf.of(6, 0, 0),
                        DefaultIntBuf.of(6, 0, 1),
                        DefaultIntBuf.of(6, 0, 2),
                        DefaultIntBuf.of(6, 1, 0),
                        DefaultIntBuf.of(6, 1, 1),
                        DefaultIntBuf.of(6, 1, 2)
                ),
                values = arrayOf(
                        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
                )
        )

        assertEquals(expected, set)

    }

    @Test
    fun `nset test`() {
        var dim =
                0(
                        2,
                        2,
                        2,
                        0(
                                2,
                                2 x 3 x 4,
                                (2 x 3)(
                                        2,
                                        3 x 4
                                )
                        )
                )
        compareNSet(dim)
    }

    @Test
    fun `nset test3`() {
        val dim = (2 x { 3 x 4 } x 4 x { 4 } x 2(3, 3))(2, 3 x 4 x { 1 }, 4)
        compareNSet(dim)
    }

    class PlainSet<T : Any>(
            val slots: Array<out IntBuf>,
            val values: Array<T>
    ) {
        val randomValues = values.toMutableList().apply {
            Collections.shuffle(this)
        }
    }

    private fun compareNSet(dim: Dimension) {
        var i = 0
        val set = nsetFrom(dim) { i++ }
        i = 0
        val set2 = mcnsetFrom(dim) { i++ }
        val slotList = arrayListOf<IntBuf>()
        val valueLIst = arrayListOf<Int>()
        i = 0
        for (index in set.indices()) {
            slotList.add(index.copy())
            valueLIst.add(i++)
        }
        val expected = PlainSet(slotList.toTypedArray(),
                                valueLIst.toTypedArray())
        assertEquals(expected, set2)
    }

    private fun <T : Any> assertEquals(expected: PlainSet<T>, set: BFSMoreCompactNSet<T>, testCopycat: Boolean = true) {
        println(set)
        expected.apply {
            //test copycat
            //test getter
            for (i in 0..slots.lastIndex)
                Assert.assertEquals(values[i], set[slots[i]])
            //test setter
            for (i in 0..slots.lastIndex)
                set[slots[i]] = randomValues[i]
            for (i in 0..slots.lastIndex)
                Assert.assertEquals(randomValues[i], set[slots[i]])
            //test indices
            var i = 0
            for (index in set.indices())
                Assert.assertTrue(slots[i++].equals(index))
            //test withIndices
            i = 0
            for ((idx, value) in set.withIndices()) {
                Assert.assertTrue(slots[i].equals(idx))
                Assert.assertEquals(randomValues[i], value)
                i++
            }
            //test iterator
            for (value in set) {

            }
        }
        if (testCopycat) {
            var i = 0
            val new_set = set.copycat { i++ }
            val new_values = arrayListOf<Int>()
            i = 0
            for (slot in expected.slots)
                new_values.add(i++)
            assertEquals(PlainSet(expected.slots, new_values.toTypedArray()), new_set as BFSMoreCompactNSet<Int>, false)
        }
    }
}