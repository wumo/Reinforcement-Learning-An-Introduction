package lab.mars.rl.model.impl

import lab.mars.rl.algo.average_alpha
import lab.mars.rl.algo.td.*
import lab.mars.rl.problem.*
import lab.mars.rl.util.argmax
import org.junit.Test

class `TD` {
    class `Blackjack problem` {
        @Test
        fun `Blackjack TD Prediction`() {
            val (prob, PI) = Blackjack.make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 500000
            val V = algo.prediction()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD average Sarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.sarsa(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD QLearning`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 100000
            val (PI, V, _) = algo.QLearning()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD average QLearning`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.QLearning(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD expectedSarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.expectedSarsa()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD average expectedSarsa`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.expectedSarsa(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD Double QLearning`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.DoubleQLearning()
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack TD average Double QLearning`() {
            val (prob, policy) = Blackjack.make()
            val algo = TemporalDifference(prob, policy)
            algo.episodes = 1000000
            val (PI, V, _) = algo.DoubleQLearning(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }
    }

    class `RandomWalk problem` {
        @Test
        fun `RandomWalk TD Prediction`() {
            val (prob, PI) = RandomWalk.make()
            val algo = TemporalDifference(prob, PI)
            algo.episodes = 1000
            val V = algo.prediction()
            prob.apply {
                for (s in states) {
                    println("${V[s].format(2)} ")
                }
            }
        }
    }

    class `Windy Grid world problem` {
        @Test
        fun `WindyGridworld TD sarsa`() {
            val prob = WindyGridworld.make()
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
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

        @Test
        fun `WindyGridworld King's Move TD sarsa`() {
            val prob = WindyGridworld.make(true)
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
            val (PI, _, _) = algo.sarsa()
            var s = prob.started[0]
            var sum = 0.0
            print(s)
            while (s.isNotTerminal()) {
                val a = argmax(s.actions) { PI[s, it] }
                val possible = a.sample()
                s = possible.next
                sum += possible.reward
                print("${WindyGridworld.desc_king_move[a[0]]}$s")
            }
            println("\nreturn=$sum")//optimal=-6
        }

        @Test
        fun `WindyGridworld TD QLearning`() {
            val prob = WindyGridworld.make()
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
            val (PI, _, _) = algo.QLearning()
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

        @Test
        fun `WindyGridworld King's Move TD QLearning`() {
            val prob = WindyGridworld.make(true)
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
            val (PI, _, _) = algo.QLearning()
            var s = prob.started[0]
            var sum = 0.0
            print(s)
            while (s.isNotTerminal()) {
                val a = argmax(s.actions) { PI[s, it] }
                val possible = a.sample()
                s = possible.next
                sum += possible.reward
                print("${WindyGridworld.desc_king_move[a[0]]}$s")
            }
            println("\nreturn=$sum")//optimal=-6
        }
    }

    class `Cliff walking problem` {
        @Test
        fun `Cliff Walking TD sarsa`() {
            val prob = CliffWalking.make()
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
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
            println("\nreturn=$sum")//optimal=-16
        }

        @Test
        fun `Cliff Walking TD Q Learning`() {
            val prob = CliffWalking.make()
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            val (PI, _, _) = algo.QLearning()
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

        @Test
        fun `Cliff Walking TD Expected Sarsa`() {
            val prob = CliffWalking.make()
            val algo = TemporalDifference(prob)
            algo.alpha = 0.5
            val (PI, _, _) = algo.expectedSarsa()
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

    class `Maximization Bias` {
        @Test
        fun `Maximization Bias Q-Learning`() {
            val prob = MaximizationBias.make()
            val algo = TemporalDifference(prob)
            algo.episodes=10
            val (PI, _, _) = algo.QLearning()
            val A = prob.started[0]
            println(PI(A))
        }

        @Test
        fun `Maximization Bias Double Q-Learning`() {
            val prob = MaximizationBias.make()
            val algo = TemporalDifference(prob)
            algo.episodes=10
            val (PI, _, _) = algo.DoubleQLearning()
            val A = prob.started[0]
            println(PI(A))
        }

    }
}