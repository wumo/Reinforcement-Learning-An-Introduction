package lab.mars.rl.util

import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.buf.IntBuf
import lab.mars.rl.util.collection.nsetOf
import lab.mars.rl.util.dimension.*
import org.junit.Assert.*
import org.junit.Test

@Suppress("UNUSED_VARIABLE", "NAME_SHADOWING", "VARIABLE_WITH_REDUNDANT_INITIALIZER")
/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class TestNSet {
  @Test
  fun `empty set`() {
    val set = nsetFrom(0) { throw Exception() }
  }
  
  @Test
  fun `terminal set`() {
    val set = nsetFrom(0(!3, !3, 2 x 3)) { println(it);0 }
    val expected = arrayOf(
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
        DefaultIntBuf.of(6, 1, 2))
    var i = 0
    for (index in set.indices()) {
      assertTrue(index.equals(expected[i++]))
    }
  }
  
  @Test
  fun `enumerate`() {
    val set = nsetFrom(2(3, 4)) { println(it);0 }
    val expected = arrayOf(
        DefaultIntBuf.of(0, 0, 0),
        DefaultIntBuf.of(0, 0, 1),
        DefaultIntBuf.of(0, 0, 2),
        DefaultIntBuf.of(0, 1, 0),
        DefaultIntBuf.of(0, 1, 1),
        DefaultIntBuf.of(0, 1, 2),
        DefaultIntBuf.of(0, 1, 3),
        DefaultIntBuf.of(1, 0, 0),
        DefaultIntBuf.of(1, 0, 1),
        DefaultIntBuf.of(1, 0, 2),
        DefaultIntBuf.of(1, 1, 0),
        DefaultIntBuf.of(1, 1, 1),
        DefaultIntBuf.of(1, 1, 2),
        DefaultIntBuf.of(1, 1, 3))
    var i = 0
    for (index in set.indices()) {
      assertTrue(index.equals(expected[i++]))
    }
  }
  
  @Test
  fun `make nset using dim2`() {
    val r1 = mutableListOf<IntArray>()
    val r2 = mutableListOf<IntArray>()
    val set = 0(3, 2 x 10 x 10).NSet { r1.add(it.toIntArray());0 }
    for (index in set.indices()) {
      r2.add((index as IntBuf).toIntArray())
    }
    r1.forEach { println(it.asList()) }
    assertArrayEquals(r1.toTypedArray(), r2.toTypedArray())
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
    val set = nsetFrom(dim) { r1.add(it.toIntArray());0 }
    for (index in set.indices()) {
      println(index)
      r2.add((index as IntBuf).toIntArray())
    }
    var i = 0
    val size = r1.size
    for (i in 0 until size) {
      println("$i\t ${r1[i].asList()} vs.${r2[i].asList()}");
    }
    assertArrayEquals(r1.toTypedArray(), r2.toTypedArray())
  }
  
  @Test
  fun `make using dimension and {}`() {
    val dim =
        (2 x { 3 x 4 } x 4 x { 4 } x 2(3, 3))(2, 3 x 4 x { 1 }, 4)
    val dim2 = 2 x 2(3)
    val dim3 = 2 x 0(3, 4, 5)
    val set = nsetFrom(dim) { println(it); 0 }
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
    val tmp = nsetFrom(2) { i++ }
    val set = nsetFrom(2 x { 3 x 4 }) { i++ }
    println(set[0, 0, 0])
    set[1] = tmp
    assertEquals(0, set[1, 0])
    assertEquals(1, set[1, 1])
    assertEquals(2, set[0, 0, 0])
  }
  
  @Test
  fun `get sub set`() {
    var i = 0
    val set = nsetFrom(2 x { 3 x 4 }) { i++ }
    for (withIndex in set.withIndices()) {
      println(withIndex)
    }
    
    for (withIndex in set(1).withIndices()) {
      println(withIndex)
    }
    
  }
  
  @Test
  fun `one level iterate`() {
    var i = 0
    val set = nsetFrom(5) { i++ }
    i = 0
    for (a in set) {
      println(a)
      assertEquals(i++, a)
    }
  }
  
  @Test
  fun `two level iterate`() {
    var i = 0
    val set = nsetFrom(5(3)) { i++ }
    i = 0
    for (a in set) {
      println(a)
      assertEquals(i++, a)
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
    val set = nsetFrom(3 x 3 x { it[it.lastIndex] + 1 }) { i++ }
    val set2 = set.copycat { println(it); it.toString() }
  }
  
  @Test
  fun `test null`() {
    val set = nsetFrom(2 x 2) { 0 }
    val result: Int? = set[0, 0]
  }
  
  @Test
  fun `inti raw with correct index and 0`() {
    var i = 0
    val exp = arrayListOf<IntBuf>()
    for (a in 0 until 3)
      for (b in 0 until 4)
        for (c in 0 until 5)
          for (d in 0 until 6)
            exp.add(DefaultIntBuf.of(a, b, c, d))
    var set = nsetFrom(3 x 4 x 5 x 6) {
      assertTrue(it.equals(exp[i])); i++
    }
    i = 0
    nsetFrom(0 x 3 x 4 x 0 x 5 x 6) {
      assertTrue(it.equals(exp[i])); i++
    }
    i = 0
    nsetFrom((3 x 4 x 5) x 6) {
      assertTrue(it.equals(exp[i])); i++
    }
    i = 0
    set = nsetFrom((3 x 4 x 5) x 6 x 0) {
      assertTrue(it.equals(exp[i])); i++
    }
    set[0, 0, 0, 0]
    i = 0
    nsetFrom(3 x (4 x 5) x 6) {
      assertTrue(it.equals(exp[i])); i++
    }
    i = 0
    set = nsetFrom(0 x (3 x 4 x 5) x 6) {
      assertTrue(it.equals(exp[i])); i++
    }
    set[0, 0, 0, 0]
    i = 0
    set = nsetFrom(0 x (3 x 4 x 5) x (6 x 0)) {
      assertTrue(it.equals(exp[i])); i++
    }
    set[0, 0, 0, 0]
  }
  
  @Test
  fun `reset`() {
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
    val set = nsetFrom<Int>(dim) { 0 }
    for (withIndex in set.withIndices()) {
      println(withIndex)
    }
    set.raw_set { _, old -> println(old);2 }
    for (withIndex in set.withIndices()) {
      println(withIndex)
    }
  }
}