package lab.mars.rl.model.impl

import lab.mars.rl.util.NSet
import lab.mars.rl.util.extension.nsetOf
import lab.mars.rl.util.invoke
import lab.mars.rl.util.nsetOf
import lab.mars.rl.util.x
import org.junit.Assert
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class TestNSet {
    @Test
    fun `make nset using dim2`() {
        val r1 = mutableListOf<IntArray>()
        val r2 = mutableListOf<IntArray>()
        val set = nsetOf<Int>(0(3, 2 x 10 x 10)) { r1.add(it.toIntArray());null }
        for (index in set.indices()) {
            r2.add(index.toIntArray())
        }
        r1.forEach { println(it.asList()) }
        Assert.assertArrayEquals(r1.toTypedArray(), r2.toTypedArray())
    }

    @Test
    fun `make nset using dim 3`() {
        val r1 = mutableListOf<IntArray>()
        val r2 = mutableListOf<IntArray>()
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
        val set = nsetOf<Int>(dim) { r1.add(it.toIntArray());null }
        for (index in set.indices()) {
            println(index)
            r2.add(index.toIntArray())
        }
        var i = 0
        val size = r1.size
        for (i in 0 until size) {
            println("${i}\t ${r1[i].asList()} vs.${r2[i].asList()}");
        }
        Assert.assertArrayEquals(r1.toTypedArray(), r2.toTypedArray())
    }

    @Test
    fun `make nset`() {
        val set = nsetOf(1, 2, 3)
        for (i in set) {
            println(i)
        }
    }

    @Test
    fun `test general shape`() {
        var i = 0
        val tmp = nsetOf<Int>(2) { i++ }
        val set = nsetOf<Int>(2) { nsetOf<Int>(3 x 4) { i++ } }
        println(set[0, 0, 0])
        set[1] = tmp
        Assert.assertEquals(0, set[1, 0])
        Assert.assertEquals(1, set[1, 1])
        Assert.assertEquals(2, set[0, 0, 0])
    }

    @Test
    fun `one level iterate`() {
        var i = 0
        val set = nsetOf<Int>(5) { i++ }
        i = 0
        for (a in set) {
            println(a)
            Assert.assertEquals(i++, a)
        }
    }


    @Test
    fun `two level iterate`() {
        var i = 0
        val set = nsetOf<Int>(5) { nsetOf<Int>(3) { i++ } }
        i = 0
        for (a in set) {
            println(a)
            Assert.assertEquals(i++, a)
        }
        for (index in set.indices()) {
            println(index)
        }
        for ((idx, s) in set.withIndices()) {
            println("$idx=$s")
        }
    }

    @Test
    fun `test copycat`() {
        var i = 0
        val set = nsetOf<Int>(3 x 3) { nsetOf<Int>(it[0] + 1) { i++ } }

        val set2 = nsetOf<String>(set) { println(it); it.toString() }
    }

    @Test
    fun `test null`() {
        val set = nsetOf<Int>(2 x 2)
        val result: Int? = set[0, 0]
    }

    @Test
    fun `inti raw with correct index and 0`() {
        var i = 0
        var set = nsetOf<Int?>(3 x 4 x 5) { idx -> println(idx); i++ }
//            set.forEach { println(it) }
//            println(set[2, 3, 4])
        set[2, 3, 4] = 100
//            println(set[2, 3, 4])
        set = nsetOf(3 x 4 x 5)
        set[0, 0, 0] = 1
//            println(set[0, 0, 0])
    }

    @Test
    fun `variational bound`() {
        var i = 0
        val set = nsetOf<Int>(3 x 4) { nsetOf<Int>(5) { i++ } }
        for (a in set) {
            print("$a,")
        }
        println()
        val a = set[2, 3, 4]
        for (a in 0 until 3)
            for (b in 0 until 4)
                for (c in 0 until 5) {
                    println(set[a, b, c])
                }
        set[2, 3, 4] = 100
        set[0, 0] = nsetOf(2) { 1 }
        println(set[0, 0, 1])
        println(set[2, 3, 4])
    }
}