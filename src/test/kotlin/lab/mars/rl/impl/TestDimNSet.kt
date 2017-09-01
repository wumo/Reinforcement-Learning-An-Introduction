package lab.mars.rl.impl

import io.kotlintest.specs.StringSpec

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class TestDimNSet : StringSpec() {
    init {
        "inti raw with corret index and 0"{
            val set = DimNSet<Int>(intArrayOf(3, 4, 5), { idx -> println(idx.toList()); 0 })
        }
    }
}