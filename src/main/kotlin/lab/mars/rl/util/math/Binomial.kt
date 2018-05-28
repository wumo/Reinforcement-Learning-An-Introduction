package lab.mars.rl.util.math

import org.apache.commons.math3.util.FastMath.exp

fun binomial(trial: Int, x: Int, p: Double): Double {
  if (trial == 0) return if (x == 0) 1.0 else 0.0
  if (x < 0 || x > trial) return 0.0
  return exp(logBinomialProbability(x, trial, p, 1.0 - p))
}