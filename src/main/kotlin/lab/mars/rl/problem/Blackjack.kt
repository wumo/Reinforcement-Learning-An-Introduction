package lab.mars.rl.problem

import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.Possible
import lab.mars.rl.model.State
import lab.mars.rl.model.impl.*
import lab.mars.rl.util.DefaultIntSlice
import lab.mars.rl.util.invoke
import lab.mars.rl.util.x
import java.util.*

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */

object Blackjack {
    private val playingCard = intArrayOf(1/*A*/, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10/*J*/, 10/*Q*/, 10/*K*/)
    private const val player_offset = 12
    private const val dealer_offset = 1

    private lateinit var win: State
    private lateinit var draw: State
    private lateinit var lose: State

    private const val usableAce_idx = 1
    private const val playerSum_idx = 2
    private const val dealShownCard_idx = 3
    private val rand = Random(System.nanoTime())
    fun make(): MDP {
        val mdp = NSetMDP(gamma = 1.0, state_dim = 0(3, 2 x 10 x 10), action_dim = { if (it[0] == 0) 0 else 2 })
        mdp.apply {
            win = states[0, 0]
            draw = states[0, 1]
            lose = states[0, 2]
            for (s in states)
                for (action in s.actions)
                    when (action[0]) {
                        0 -> sticks(action, s)
                        1 -> hits(action, s)
                    }
        }
        return mdp
    }

    private fun MDP.sticks(action: Action, s: State) {
        action.sample = {
            var dealer = s[dealShownCard_idx] + dealer_offset
            var usableAceDealer = dealer == 1
            if (usableAceDealer)
                dealer += 10
            while (dealer < 17) {
                val card = drawCard()
                dealer += card
                if (dealer > 21 && usableAceDealer) {
                    dealer -= 10
                    usableAceDealer = false
                }
            }
            if (dealer <= 21) {
                val player = s[playerSum_idx] + player_offset
                when {
                    player > dealer -> Possible(win, 1.0, 1.0)
                    player == dealer -> Possible(draw, 0.0, 1.0)
                    player < dealer -> Possible(lose, -1.0, 1.0)
                    else -> throw Exception("impossible")
                }
            } else//deal goes bust
                Possible(win, 1.0, 1.0)
        }
    }

    private fun MDP.hits(action: Action, s: State) {
        action.sample = {
            var player = s[playerSum_idx] + player_offset
            val card = drawCard()
            player += card
            when {
                player <= 21 -> {
                    val idx = DefaultIntSlice.from(s)
                    idx[playerSum_idx] = player - player_offset
                    Possible(states[idx], 0.0, 1.0)
                }
                s[usableAce_idx] == 0 -> Possible(lose, -1.0, 1.0)
                else -> {
                    player -= 10
                    val idx = DefaultIntSlice.from(s)
                    idx[usableAce_idx] = 0
                    idx[playerSum_idx] = player - player_offset
                    Possible(states[idx], 0.0, 1.0)
                }
            }
        }
    }


    private fun drawCard(): Int {
        val index = rand.nextInt(playingCard.size)
        val card = playingCard[index]
        return card
    }
}