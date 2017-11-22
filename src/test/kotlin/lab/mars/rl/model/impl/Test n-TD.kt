package lab.mars.rl.model.impl

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.algo.ntd.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.problem.*
import lab.mars.rl.util.Rand
import lab.mars.rl.util.argmax
import org.junit.Test


class `n-step TD` {
    class `Blackjack problem` {
        @Test
        fun `Blackjack n-TD Prediction`() {
            val (prob, `π`) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, 102400, `π`)
            algo.episodes = 500000
            val V = algo.prediction()
            printBlackjack(prob, `π`, V)
        }

        @Test
        fun `Blackjack n-TD Sarsa`() {
            val (prob, `π`) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, `π`)
            algo.α = 0.1
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD average Sarsa`() {
            val (prob, `π`) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, `π`)
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD off-policy Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.α = 0.1
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy sarsa`()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD average off-policy Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy sarsa`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD treebackup`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, 4, policy)
            algo.α = 0.1
            algo.episodes = 1000000
            val (PI, V, _) = algo.treebackup()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD average treebackup`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.treebackup(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q(σ) σ=0`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.σ = { 0 }//相当于treebackup
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q(σ) σ=1`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.σ = { 1 }//相当于off-policy sarsa?但结果似乎不像
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q(σ) σ=%2`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.σ = { it % 2 }
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q(σ) σ=random`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.σ = { Rand().nextInt(2) }
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy n-step Q(σ)`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }
    }

    class `Randomwalk Problem` {
        @Test
        fun `RandomWalk n-TD Prediction`() {
            val (prob, PI) = RandomWalk.make()
            val algo = NStepTemporalDifference(prob, 8, PI)
            algo.episodes = 1000
            val V = algo.prediction()
            prob.apply {
                for (s in states) {
                    println("${V[s].format(2)} ")
                }
            }
        }

    }

    class `Windy grid world problem` {
        @Test
        fun `WindyGridworld n-TD sarsa`() {
            val prob = WindyGridworld.make()
            val algo = NStepTemporalDifference(prob, 10)
            algo.α = 0.1
            algo.episodes = 1000000
            val (PI, _, _) = algo.sarsa(average_alpha(prob))
            var s = prob.started[0]
            var sum = 0.0
            print(s)
            while (s.isNotTerminal()) {
                val a = argmax(s.actions) { PI[s, it] }
                val possible = a.sample()
                s = possible.next
                sum += possible.reward
                print("${WindyGridworld.desc_move[a[0]]}$s")
            }
            println("\nreturn=$sum")//optimal=-14
        }
    }

    class `Cliff walking problem` {
        @Test
        fun `Cliff Walking n-TD off-policy sarsa`() {
            val prob = CliffWalking.make()
            val algo = NStepTemporalDifference(prob, 10)
            algo.α = 0.5
            val (PI, _, _) = algo.`off-policy sarsa`()
            var s = prob.started[0]
            var sum = 0.0
            print(s)
            while (s.isNotTerminal()) {
                val a = argmax(s.actions) { PI[s, it] }
                val possible = a.sample()
                s = possible.next
                sum += possible.reward
                print("${WindyGridworld.desc_move[a[0]]}$s")
            }
            println("\nreturn=$sum")//optimal=-12
        }
    }
}
