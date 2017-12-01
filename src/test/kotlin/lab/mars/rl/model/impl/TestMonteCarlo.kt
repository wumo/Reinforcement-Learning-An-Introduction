package lab.mars.rl.model.impl

import lab.mars.rl.algo.mc.*
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.RandomWalk
import org.junit.jupiter.api.Test

class `MC` {
    class `Blackjack Problem` {
        val result = """
    -0.39 -0.43 -0.46 -0.48 -0.52 -0.53 -0.54 -0.56 0.15 0.63
    -0.20 -0.30 -0.29 -0.34 -0.35 -0.39 -0.42 -0.44 0.65 0.88
    -0.21 -0.28 -0.30 -0.34 -0.35 -0.37 -0.39 -0.44 0.68 0.89
    -0.20 -0.22 -0.31 -0.33 -0.37 -0.38 -0.40 -0.43 0.69 0.88
    -0.24 -0.27 -0.32 -0.34 -0.37 -0.39 -0.44 -0.42 0.68 0.89
    -0.26 -0.27 -0.27 -0.34 -0.35 -0.39 -0.42 -0.45 0.71 0.90
    -0.24 -0.26 -0.27 -0.30 -0.35 -0.36 -0.40 -0.42 0.78 0.92
    -0.18 -0.27 -0.30 -0.32 -0.33 -0.37 -0.40 -0.42 0.79 0.94
    -0.21 -0.23 -0.29 -0.29 -0.34 -0.37 -0.36 -0.42 0.77 0.94
    -0.30 -0.31 -0.34 -0.35 -0.40 -0.43 -0.45 -0.47 0.45 0.89
    ------------------------------------------------------------
    -0.64 -0.67 -0.68 -0.69 -0.73 -0.75 -0.77 -0.79 0.14 0.63
    -0.53 -0.57 -0.61 -0.62 -0.66 -0.67 -0.71 -0.72 0.65 0.88
    -0.54 -0.57 -0.61 -0.63 -0.66 -0.67 -0.70 -0.73 0.66 0.89
    -0.55 -0.57 -0.60 -0.64 -0.66 -0.69 -0.71 -0.73 0.66 0.89
    -0.53 -0.58 -0.61 -0.64 -0.66 -0.69 -0.71 -0.74 0.67 0.89
    -0.54 -0.55 -0.61 -0.63 -0.66 -0.68 -0.71 -0.72 0.70 0.91
    -0.50 -0.56 -0.58 -0.63 -0.66 -0.65 -0.69 -0.72 0.79 0.93
    -0.52 -0.55 -0.59 -0.61 -0.63 -0.66 -0.70 -0.71 0.80 0.93
    -0.51 -0.57 -0.58 -0.65 -0.65 -0.65 -0.69 -0.72 0.75 0.93
    -0.57 -0.58 -0.61 -0.64 -0.68 -0.71 -0.73 -0.73 0.44 0.89
    """

        @Test
        fun `Blackjack MC Prediction`() {
            val (prob, PI) = Blackjack.make()
            val algo = MonteCarlo(prob, PI)
            algo.episodes = 500000
            val V = algo.prediction()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Off-policy MC Prediction`() {
            val (prob, PI) = Blackjack.make()
            val algo = MonteCarlo(prob, PI)
            algo.episodes = 500000
            val V = algo.`Off-policy MC prediction`()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack MC Optimal`() {
            val (prob, policy1) = Blackjack.make()
            val algo = MonteCarlo(prob, policy1)
            algo.episodes = 1000000
            val (PI, V, _) = algo.`Optimal Exploring Starts`()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Optimal On-policy first-visit MC control`() {
            val (prob, policy1) = Blackjack.make()
            val algo = MonteCarlo(prob, policy1)
            algo.episodes = 1000000
            val (PI, V, _) = algo.`On-policy first-visit MC control`()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Optimal Off-policy MC control`() {
            val (prob, policy1) = Blackjack.make()
            val algo = MonteCarlo(prob, policy1)
            algo.episodes = 1000000
            val (PI, V, _) = algo.`Off-policy MC Optimal`()
            printBlackjack(prob, PI, V)
        }
    }

    class `Randomwalk problem` {
        @Test
        fun `RandomWalk MC Prediction`() {
            val (prob, PI) = RandomWalk.make()
            val algo = MonteCarlo(prob, PI)
            algo.episodes = 1000
            val V = algo.prediction()
            prob.apply {
                for (s in states) {
                    println("${V[s].format(2)} ")
                }
            }
        }
    }
}
