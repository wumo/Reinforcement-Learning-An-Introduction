package lab.mars.rl.model.impl.func

import lab.mars.rl.util.matrix.Matrix

val ClosedRange<Double>.size: Double
    get() = endInclusive - start

class SimpleCoarseCoding(featureWidth: Double, domain: ClosedRange<Double>,
                         override val numOfComponents: Int, converter: (Array<out Any>) -> Double) : Feature<Double>(converter) {
    val features: Array<ClosedRange<Double>>

    init {
        val step = (domain.size - featureWidth) / (numOfComponents - 1)
        var left = domain.start
        features = Array(numOfComponents) {
            (left..(left + featureWidth)).apply { left += step }
        }
    }

    override fun _invoke(s: Double) = Matrix.column(numOfComponents) {
        if (features[it].contains(s)) 1.0 //quantize the interval
        else 0.0
    }

}