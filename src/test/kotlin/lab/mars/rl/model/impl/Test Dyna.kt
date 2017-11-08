package lab.mars.rl.model.impl

import javafx.application.Application
import lab.mars.rl.algo.average_alpha
import lab.mars.rl.algo.dyna.*
import lab.mars.rl.problem.*
import lab.mars.rl.util.argmax
import lab.mars.rl.util.ui.GridWorldUI
import lab.mars.rl.util.ui.RodManeuveringUI
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class `Dyna` {
    class `Blackjack Problem` {
        @Test
        fun `Blackjack RandomSampleOneStepTabularQLearning`() {
            val (prob, _) = Blackjack.make()
            val algo = RandomSampleOneStepTabularQLearning(prob)
            algo.episodes = 1000000
            val (PI, V, _) = algo.optimal(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Dyna-Q`() {
            val (prob, _) = Blackjack.make()
            val algo = DynaQ(prob)
            algo.episodes = 100000
            algo.n = 10
            val (PI, V, _) = algo.optimal(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Prioritized Sweeping Stochastic`() {
            val (prob, _) = Blackjack.make()
            val algo = PrioritizedSweepingStochasticEnv(prob)
            algo.episodes = 100000
            algo.n = 10
            val (PI, V, _) = algo.optimal(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }

        @Test
        fun `Blackjack Dyna-Q on-policy`() {
            val (prob, _) = Blackjack.make()
            val algo = `Dyna-Q-OnPolicy`(prob)
            algo.episodes = 1000000
            algo.n = 10
            val (PI, V, _) = algo.optimal(average_alpha(prob))
            printBlackjack(prob, PI, V)
        }
    }

    class `Dyna Maze` {

        @Test
        fun `Dyna Q UI`() {
            val prob = DynaMaze.make()
            val algo = DynaQ(prob)
            algo.episodes = 1000
            algo.n = 10
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    GridWorldUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("${DynaMaze.desc_move[a[0]]}$s")
                }
                println("\nsteps=$count")//optimal=14
            }
            GridWorldUI.after = { latch.countDown() }
            Application.launch(GridWorldUI::class.java)
        }

        @Test
        fun `Dyna Q+ UI`() {
            val prob = DynaMaze.make()
            val algo = `Dyna-Q+`(prob)
            algo.episodes = 1000
            algo.n = 10
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    GridWorldUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("${DynaMaze.desc_move[a[0]]}$s")
                }
                println("\nsteps=$count")//optimal=14
            }
            GridWorldUI.after = { latch.countDown() }
            Application.launch(GridWorldUI::class.java)
        }

        @Test
        fun `Dyna Q Prioritized Sweeping UI`() {
            val prob = DynaMaze.make()
            val algo = PrioritizedSweeping(prob)
            algo.episodes = 1000
            algo.n = 10
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    GridWorldUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("${DynaMaze.desc_move[a[0]]}$s")
                }
                println("\nsteps=$count")//optimal=14
            }
            GridWorldUI.after = { latch.countDown() }
            Application.launch(GridWorldUI::class.java)
        }

        @Test
        fun `Dyna Q Prioritized Sweeping Stochastic UI`() {
            val prob = DynaMaze.make()
            val algo = PrioritizedSweepingStochasticEnv(prob)
            algo.episodes = 1000
            algo.n = 10
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    GridWorldUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("${DynaMaze.desc_move[a[0]]}$s")
                }
                println("\nsteps=$count")//optimal=14
            }
            GridWorldUI.after = { latch.countDown() }
            Application.launch(GridWorldUI::class.java)
        }

        @Test
        fun `Dyna Q Example On-Policy Trajectory Sampling UI`() {
            val prob = DynaMaze.make()
            val algo = `Dyna-Q-OnPolicy`(prob)
            algo.episodes = 1000
            algo.n = 20
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    GridWorldUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("${DynaMaze.desc_move[a[0]]}$s")
                }
                println("\nsteps=$count")//optimal=14
            }
            GridWorldUI.after = { latch.countDown() }
            Application.launch(GridWorldUI::class.java)
        }

        @Test
        fun `Dyna Q`() {
            val prob = DynaMaze.make()
            val algo = DynaQ(prob)
            algo.episodes = 1000
            algo.n = 0
            val (PI, _, _) = algo.optimal()
            var s = prob.started[0]
            var count = 0
            print(s)
            while (s.isNotTerminal()) {
                val a = argmax(s.actions) { PI[s, it] }
                val possible = a.sample()
                s = possible.next
                count++
                print("${DynaMaze.desc_move[a[0]]}$s")
            }
            println("\nsteps=$count")//optimal=12
        }
    }

    class `Windy Gridworld` {
        @Test
        fun `WindyGridworld Prioritized Sweeping`() {
            val prob = WindyGridworld.make()
            val algo = PrioritizedSweeping(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
            val (PI, _, _) = algo.optimal()
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

    class `Cliff Walking` {
        @Test
        fun `Cliff Walking TD Q Learning`() {
            val prob = CliffWalking.make()
            val algo = PrioritizedSweeping(prob)
            algo.alpha = 0.5
            algo.episodes = 1000
            val (PI, _, _) = algo.optimal()
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

    class `Rod maneuvering` {
        @Test
        fun `PrioritizedSweeping`() {
            val prob = RodManeuvering.make()
            val algo = PrioritizedSweeping(prob)
            algo.episodes = 1000
            algo.n = 10
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    RodManeuveringUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("$a$s")
                }
                println("\nsteps=$count")//optimal=39
            }
            RodManeuveringUI.after = { latch.countDown() }
            Application.launch(RodManeuveringUI::class.java)
        }

        @Test
        fun `Example On-Policy Trajectory Sampling`() {
            val prob = RodManeuvering.make()
            val algo = `Dyna-Q-OnPolicy`(prob)
            algo.episodes = 1000
            algo.n = 20
            val latch = CountDownLatch(1)

            thread {
                latch.await()
                algo.stepListener = { V, s ->
                    RodManeuveringUI.render(V, s)
                }
                val (PI, _, _) = algo.optimal()
                var s = prob.started[0]
                var count = 0
                print(s)
                while (s.isNotTerminal()) {
                    val a = argmax(s.actions) { PI[s, it] }
                    val possible = a.sample()
                    s = possible.next
                    count++
                    print("$a$s")
                }
                println("\nsteps=$count")//optimal=39
            }
            RodManeuveringUI.after = { latch.countDown() }
            Application.launch(RodManeuveringUI::class.java)
        }
    }
}