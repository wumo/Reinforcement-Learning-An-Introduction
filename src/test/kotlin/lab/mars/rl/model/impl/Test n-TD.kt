package lab.mars.rl.model.impl

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.algo.ntd.*
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.CliffWalking
import lab.mars.rl.problem.RandomWalk
import lab.mars.rl.problem.WindyGridworld
import lab.mars.rl.util.Rand
import lab.mars.rl.util.argmax
import org.junit.Test


class `n-step TD` {
    class `Blackjack problem` {
        @Test
        fun `Blackjack n-TD Prediction`() {
            val (prob, PI) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, 102400, PI)
            algo.episodes = 500000
            val V = algo.prediction()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.alpha = 0.1
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD average Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD off-policy Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.alpha = 0.1
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
            algo.alpha = 0.1
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
        fun `Blackjack n-TD Q sigma=0`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.sig = { 0 }//相当于treebackup
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy Q sigma`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q sigma=1`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.sig = { 1 }//相当于off-policy sarsa?但结果似乎不像
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy Q sigma`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q sigma=%2`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.sig = { it % 2 }
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy Q sigma`(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack n-TD Q sigma=random`() {
            val (prob, policy) = Blackjack.make()
            val algo = NStepTemporalDifference(prob, Int.MAX_VALUE, policy)
            algo.sig = { Rand().nextInt(2) }
            algo.episodes = 1000000
            val (PI, V, _) = algo.`off-policy Q sigma`(average_alpha(prob))
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
            algo.alpha = 0.1
            algo.episodes = 10000
            val (PI, _, _) = algo.sarsa()
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
            algo.alpha = 0.5
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
