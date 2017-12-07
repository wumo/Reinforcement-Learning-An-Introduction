package lab.mars.rl.util.range

import kotlin.math.sign

class DoubleProgression(val start: Double,
                        val endInclusive: Double,
                        val step: Double): Iterable<Double> {
  override fun iterator() = object: Iterator<Double> {
    var current = start
    override fun hasNext() = step.sign * (endInclusive - current) >= 0
    
    override fun next() = current.apply { current += step }
  }
}

operator fun Double.rangeTo(that: Double)
    = DoubleProgression(this, that, 0.1)

infix fun DoubleProgression.step(step: Double)
    = DoubleProgression(start, endInclusive, step)