package lab.mars.rl.model.impl

import lab.mars.rl.model.State

val ClosedRange<Double>.size: Double
    get() = endInclusive - start

class SimpleCoarseCoding(featureWidth: Double, domain: ClosedRange<Double>, val scale: Double,
                         override val featureNum: Int) : featureFunc {
    val features: Array<ClosedRange<Double>>

    init {
        val step = (domain.size - featureWidth) / (featureNum - 1)
        var left = domain.start
        features = Array(featureNum) {
            (left..(left + featureWidth)).apply { left += step }
        }
    }

    override fun invoke(s: State) = DoubleArray(featureNum) {
        if (features[it].contains(s[0].toDouble() * scale)) 1.0 //quantize the interval
        else 0.0
    }

    override fun alpha(alpha: Double, s: State) =
            alpha / features.sumBy { if (it.contains(s[0].toDouble() * scale)) 1 else 0 }
}