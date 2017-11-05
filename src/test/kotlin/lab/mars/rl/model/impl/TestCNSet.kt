package lab.mars.rl.model.impl

import lab.mars.rl.util.CompactNSet
import lab.mars.rl.util.CompactNSet.Cell
import lab.mars.rl.util.CompactNSet.SubTree
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.buf.buf
import lab.mars.rl.util.cnsetOf
import lab.mars.rl.util.dimension.*
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
        val data = arrayOf(Cell(arrayOf(SubTree(2, 1, 4), SubTree(3, 2, 4)).buf(), 1), 5,2, Cell(arrayOf(SubTree(2, 4, 4)).buf(), 3), 4)
        val set = CompactNSet<Int>(data.buf())
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
    fun `subset`() {
        val data = arrayOf(Cell(arrayOf(SubTree(2, 4, 4), SubTree(3, 1, 3)).buf(), 1), 2, Cell(arrayOf(SubTree(2, 3, 3)).buf(), 3), 4, 5)
        val set = CompactNSet<Int>(data.buf())
        val subset = set(1)
        println(set[1])
        print(subset)
        println(subset[0])
    }

    @Test
    fun `test cnsetOf 1`() {
        val set = cnsetOf(1)
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0)
                ),
                values = arrayOf(
                        1
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `test cnsetOf`() {
        val set = cnsetOf(1, 2, 3)
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0),
                        DefaultIntBuf.of(1),
                        DefaultIntBuf.of(2)
                ),
                values = arrayOf(
                        1, 2, 3
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using GeneralDimension 3`() {
        var i = 0
        val set = cnsetFrom(3) { i++ }
        Assert.assertEquals(3, set.size)
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
        val set = cnsetFrom(2 x 3) { i++ }
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
        val set = cnsetFrom(2 x 3 x 4) { i++ }
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
    fun `make using GeneralDimension 2 x 3 x 3`() {
        var i = 0
        val set = cnsetFrom(2 x 3 x 3) { i++ }
        i = 0
        val slotList = arrayListOf<IntBuf>()
        val valuesList = arrayListOf<Int>()
        for (a in 0 until 2)
            for (b in 0 until 3)
                for (c in 0 until 3) {
                    slotList.add(DefaultIntBuf.of(a, b, c))
                    valuesList.add(i++)
                }

        val expected = PlainSet(
                slotList.toTypedArray(),
                valuesList.toTypedArray()
        )

        assertEquals(expected, set)
        val set2=set(0)
        println(set2.size)
    }

    @Test
    fun `make using GeneralDimension 0(3, 2 x 2)`() {
        var i = 0
        val set = cnsetFrom(0(3, 2 x 2)) { i++ }
        print(set)
        val expected = PlainSet(
                slots = arrayOf(
                        DefaultIntBuf.of(0, 0),
                        DefaultIntBuf.of(0, 1),
                        DefaultIntBuf.of(0, 2),
                        DefaultIntBuf.of(1, 0, 0),
                        DefaultIntBuf.of(1, 0, 1),
                        DefaultIntBuf.of(1, 1, 0),
                        DefaultIntBuf.of(1, 1, 1)
                ),
                values = arrayOf(
                        0, 1, 2, 3, 4, 5, 6
                )
        )

        assertEquals(expected, set)
    }

    @Test
    fun `make using GeneralDimension 2 x { it+1 }`() {
        var i = 0
        val set = cnsetFrom(2 x { it[0] + 1 }) { i++ }

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
        val set = cnsetFrom(0(!3)) { i++ }
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
        val set = cnsetFrom(0(!3, !3)) { i++ }
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
        val set = cnsetFrom(0(!3, !3, 2 x 3)) { i++ }
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
        val dim =
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
    fun `nset test2`() {
        val dim = (1 x { 1 x 2 })(1 x 1)
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
        println(set)
        i = 0
        val set2 = cnsetFrom(dim) { i++ }
        val slotList = arrayListOf<IntBuf>()
        val valueLIst = arrayListOf<Int>()
        i = 0
        for (index in set.indices()) {
            slotList.add((index as IntBuf).copy())
            valueLIst.add(i++)
        }
        val expected = PlainSet(slotList.toTypedArray(),
                                valueLIst.toTypedArray())
        assertEquals(expected, set2)
    }

    private fun <T : Any> assertEquals(expected: PlainSet<T>, set: CompactNSet<T>, testCopycat: Boolean = true) {
        println(set)
        expected.apply {
            Assert.assertEquals("set size is wrong!", values.size, set.size)
            //test getter
            for (i in 0..slots.lastIndex)
                Assert.assertEquals("set getter is wrong!", values[i], set[slots[i]])
            //test setter
            for (i in 0..slots.lastIndex)
                set[slots[i]] = randomValues[i]
            for (i in 0..slots.lastIndex)
                Assert.assertEquals("set setter is wrong!", randomValues[i], set[slots[i]])
            //test indices
            var i = 0
            for (index in set.indices())
                Assert.assertEquals("set index is wrong!", slots[i++], index)
            //test withIndices
            i = 0
            for ((idx, value) in set.withIndices()) {
                Assert.assertEquals("set.withIndices index is wrong!", slots[i], idx)
                Assert.assertEquals("set.withIndices value  is wrong!", randomValues[i], value)
                i++
            }
            val hashValues = hashSetOf<T>()
            hashValues.addAll(values)
            //test iterator
            for (value in set) {
                Assert.assertTrue(hashValues.contains(value))
            }
            //test dfs

            //test subset
            for (a in 0..slots.lastIndex) {
                val slot = slots[a]
                val last = slot.lastIndex
                for (b in 0 until last - 1)
                    Assert.assertEquals(randomValues[a], set(slot[0, b])[slot[b + 1, last]])
            }
        }
        if (testCopycat) {
            var i = 0
            val new_set = set.copycat { i++ }
            val new_values = arrayListOf<Int>()
            i = 0
            for (slot in expected.slots)
                new_values.add(i++)
            assertEquals(PlainSet(expected.slots, new_values.toTypedArray()), new_set as CompactNSet<Int>, false)
        }
    }
}