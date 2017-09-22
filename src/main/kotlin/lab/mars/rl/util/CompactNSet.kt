package lab.mars.rl.util

/**
 * <p>
 * Created on 2017-09-18.
 * </p>
 *
 * @author wumo
 */
class CompactNSet<T : Any> private constructor(private val data: Array<Any>, private val rootSetInfo: SetInfo) : RandomAccessCollection<T>() {

    /**
     * @param range 包含元素数
     * @param secondValueOffset 第二个元素开始的位置
     * @param firstValueOffset  第一个元素的偏置（单独列出来的原因是，第一个元素可能保存在父节点上，另外第一个元素如果是集合，那就是父节点）
     */
    private class SetInfo(val range: Int, val secondValueOffset: Int, val firstValueOffset: Int) {

    }

    /**
     * @param levels 此[SubSet]同时记录的多个[SubSet]
     * @param baseLevel 此[SubSet]起始的level
     * @param value 包含的元素值（或者说此[SubSet]的起始元素值）
     * @param valueLevel 包含的元素值所在层数（即只在制定层数才能获取这个元素值）
     */
    private class SubSet<T : Any>(val levels: Array<SetInfo>, val baseLevel: Int, var value: T, val valueLevel: Int) {
        operator fun get(level: Int): SetInfo {
            if (level < baseLevel || level >= baseLevel + levels.size)
                throw IndexOutOfDimensionException()
            return levels[level - baseLevel]
        }
    }

    override fun <T : Any> copycat(element_maker: (IntBuf) -> T): RandomAccessCollection<T> {
        TODO("not implemented")
    }

    private fun <T : Any> get_or_set(idxIter: Iterator<Int>, op: (Any) -> Any): T {
        var level = -1
        var parentSet = rootSetInfo
        var tmp: Any
        outer@ while (true) {
            val d = idxIter.next()
            level++
            if (d < 0 || d >= parentSet.range)
                throw IndexOutOfDimensionException()
            when {
                idxIter.hasNext() -> {
                    if (d == 0)
                        continue@outer
                    else {
                        tmp = data[parentSet.secondValueOffset + d - 1]
                        when (tmp) {//遍历子集
                            is SubSet<*> -> parentSet = tmp[level]
                            else -> throw IndexOutOfDimensionException()
                        }
                    }
                }
                d == 0 -> {
                    tmp = data[parentSet.firstValueOffset]
                    tmp as SubSet<*>
                    return (if (level == tmp.valueLevel) tmp.value
                    else tmp[level]) as T
                }
                else -> { //获取元素
                    tmp = data[parentSet.secondValueOffset + d - 1]
                    return when (tmp) {
                        is SubSet<*> -> {
                            if (tmp.valueLevel != level)
                                throw IndexOutOfDimensionException()
                            tmp.value as T
                        }
                        else -> tmp as T
                    }
                }
            }
        }
    }

    override fun <T : Any> _get(idx: Index): T {
        TODO("not implemented")
    }

    override fun <T : Any> _set(idx: Index, s: T) {
        TODO("not implemented")
    }

    override fun indices(): Iterator<IntBuf> {
        TODO("not implemented")
    }

    override fun withIndices(): Iterator<Pair<out IntBuf, T>> {
        TODO("not implemented")
    }


    override fun iterator(): Iterator<T> {
        TODO("not implemented")
    }

}