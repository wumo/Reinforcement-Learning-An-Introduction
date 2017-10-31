package lab.mars.rl.model.impl

import javafx.application.Application
import lab.mars.rl.algo.average_alpha
import lab.mars.rl.algo.dyna.DynaQ
import lab.mars.rl.algo.dyna.PrioritizedSweeping
import lab.mars.rl.algo.dyna.RandomSampleOneStepTabularQLearning
import lab.mars.rl.algo.dyna.`Dyna-Q+`
import lab.mars.rl.problem.Blackjack
import lab.mars.rl.problem.DynaMaze
import lab.mars.rl.util.UI
import lab.mars.rl.util.argmax
import org.junit.Test
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


class `Dyna` {
    class `Blackjack Problem` {
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
        fun `Blackjack RandomSampleOneStepTabularQLearning`() {
            val (prob, _) = Blackjack.make()
            val algo = RandomSampleOneStepTabularQLearning(prob)
            algo.episodes = 1000000
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
                    UI.render(V, s)
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
            UI.after = { latch.countDown() }
            Application.launch(UI::class.java)
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
                    UI.render(V, s)
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
            UI.after = { latch.countDown() }
            Application.launch(UI::class.java)
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
                    UI.render(V, s)
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
            UI.after = { latch.countDown() }
            Application.launch(UI::class.java)
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
}