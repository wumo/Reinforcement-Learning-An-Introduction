package lab.mars.rl.model.impl

import lab.mars.rl.model.MDP
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
        val set = NSet<Int>(1(3, 2 x 10 x 10)) { r1.add(it.toIntArray());null }
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
        val set = NSet<Int>(dim) { r1.add(it.toIntArray());null }
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
    fun `make nset using dim`() {
        0(
                2,
                2 x 3 x 10,
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

        o(2)
        o(2 x 3)
        o {
            o {
                o {

                }
            }
        }
        o(2 x 3) {
            o(2 x 3)
            o(1 x 2 x 3) {
                o(2 x 3)
            }
            o {
                o(2)
                o(2 x 3)
            }

            o[1, 2] = 1
            o[0, 0] = {
                o(2)
                o(2 x 3)
                o {
                    o(2)
                    o(2 x 3)
                }
            }
            o[1, 0] = x
            o[0, 1] = x

            except {
                o[1, 2] = 1
                o[0, 1] = (2 x 3) {

                }
                o[0, 0] = {
                    o(2)
                    o(2 x 3)
                    o {
                        o(2)
                        o(2 x 3)
                    }
                }
                o[1, 0] = x
                o[0, 1] = x
            }
        }
    }

    @Test
    fun `make nset`() {
        val set = NSet.of(1, 2, 3)
        for (i in set) {
            println(i)
        }
    }

    @Test
    fun `test general shape`() {
        var i = 0
        val tmp = NSet<Int>(2) { i++ }
        val set = NSet<Int>(2) { NSet<Int>(3, 4) { i++ } }
        println(set[0, 0, 0])
        set[1] = tmp
        Assert.assertEquals(0, set[1, 0])
        Assert.assertEquals(1, set[1, 1])
        Assert.assertEquals(2, set[0, 0, 0])
    }

    @Test
    fun `one level iterate`() {
        var i = 0
        val set = NSet<Int>(5) { i++ }
        i = 0
        for (a in set) {
            println(a)
            Assert.assertEquals(i++, a)
        }
    }


    @Test
    fun `two level iterate`() {
        var i = 0
        val set = NSet<Int>(5) { NSet<Int>(3) { i++ } }
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
        val set = NSet<Int>(3, 3) { NSet<Int>(it[0] + 1) { i++ } }

        val set2 = NSet<String>(set) { println(it); it.toString() }
    }

    @Test
    fun `test null`() {
        val set = NSet<Int>(2, 2)
        val result: Int? = set[0, 0]
    }

    @Test
    fun `inti raw with correct index and 0`() {
        var i = 0
        var set = NSet<Int?>(3, 4, 5) { idx -> println(idx); i++ }
//            set.forEach { println(it) }
//            println(set[2, 3, 4])
        set[2, 3, 4] = 100
//            println(set[2, 3, 4])
        set = NSet(3, 4, 5)
        set[0, 0, 0] = 1
//            println(set[0, 0, 0])
    }

    @Test
    fun `variational bound`() {
        var i = 0
        val set = NSet<Int>(3, 4) { NSet<Int>(5) { i++ } }
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
        set[0, 0] = NSet(2) { 1 }
        println(set[0, 0, 1])
        println(set[2, 3, 4])
    }

    @Test
    fun `NSetMDP`() {
        val mdp = NSetMDP(gamma = 1.0, state_dim = 3 x 4 x 5, action_dim = 4)
        val S = mdp.states
        val V = mdp.v_maker()
        val PI = mdp.pi_maker()
        val Q = mdp.q_maker()
        val index = intArrayOf(2, 3, 4)
        S.forEach { s ->
            println(s)
            V[s] = 1.0
            val a = s.actions.firstOrNull()
            PI[s] = s.actions.first()
            if (a == null) return@forEach
            Q[s, a] = 1.0
        }
    }
}

val dim = intArrayOf(2, 3, 4)
val mdp = MDP(
        gamma = 0.9,
        states = NSet(*dim),
        v_maker = { NSet(*dim) { 0.0 } },
        q_maker = { NSet(*dim, 4) { 0.0 } },
        pi_maker = { NSet(*dim, 4) })
