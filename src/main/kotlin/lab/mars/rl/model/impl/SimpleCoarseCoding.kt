package lab.mars.rl.model.impl

import lab.mars.rl.model.IndexedState
import lab.mars.rl.util.matrix.Matrix

val ClosedRange<Double>.size: Double
    get() = endInclusive - start

class SimpleCoarseCoding(featureWidth: Double, domain: ClosedRange<Double>, val scale: Double,
                         override val numOfComponents: Int) : Feature {
    val features: Array<ClosedRange<Double>>

    init {
        val step = (domain.size - featureWidth) / (numOfComponents - 1)
        var left = domain.start
        features = Array(numOfComponents) {
            (left..(left + featureWidth)).apply { left += step }
        }
    }

    override fun invoke(s: IndexedState) = Matrix.column(numOfComponents) {
        if (features[it].contains(s[0].toDouble() * scale)) 1.0 //quantize the interval
        else 0.0
    }

    override fun alpha(alpha: Double, s: IndexedState) =
            alpha / features.sumBy { if (it.contains(s[0].toDouble() * scale)) 1 else 0 }
}