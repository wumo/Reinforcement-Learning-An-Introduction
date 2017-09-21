package lab.mars.rl.problem

import lab.mars.rl.model.*
import lab.mars.rl.model.impl.*
import lab.mars.rl.util.DefaultIntBuf
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
    private lateinit var win: State
    private lateinit var draw: State
    private lateinit var lose: State

    private const val reward_win = 1.0
    private const val reward_draw = 0.0
    private const val reward_lose = -1.0

    private const val ace_idx = 1
    private const val dealer_idx = 2
    private const val player_idx = 3
    private const val player_offset = 12
    private const val dealer_offset = 1
    private val rand = Random(System.nanoTime())

    fun make(): Pair<MDP, DeterminedPolicy> {
        val mdp = NSetMDP(gamma = 1.0, state_dim = 0(3, 2 x 10 x 10), action_dim = { if (it[0] == 0) 0 else 2 })
        mdp.apply {
            win = states[0, 0]
            draw = states[0, 1]
            lose = states[0, 2]
            for (s in states(1))
                for (a in s.actions)
                    when (a[0]) {
                        0 -> a.sample = sticks(s)
                        1 -> a.sample = hits(s)
                    }
        }
        val policy1 = mdp.stateFunc<Action> { null_action }
        for (s in mdp.states(1))
            if (s[player_idx] >= 20 - player_offset)
                policy1[s] = s.actions[0]
            else
                policy1[s] = s.actions[1]
        return Pair(mdp, policy1)
    }

    private fun MDP.sticks(s: State) = {
        var dealer = s[dealer_idx] + dealer_offset
        var usableAceDealer = dealer == 1
        //前两张牌决定是否是Ace
        if (usableAceDealer)
            dealer += 10
        else {
            val card = drawCard()
            dealer += card
            if (card == 1) {
                usableAceDealer = true
                dealer += 10
            }
        }
        while (dealer < 17) {
            val card = drawCard()
            dealer += card
            if (dealer > 21 && usableAceDealer) {
                dealer -= 10
                usableAceDealer = false
            }
        }
        if (dealer <= 21) {
            val player = s[player_idx] + player_offset
            when {
                player > dealer -> Possible(win, reward_win, 1.0)
                player == dealer -> Possible(draw, reward_draw, 1.0)
                player < dealer -> Possible(lose, reward_lose, 1.0)
                else -> throw Exception("impossible")
            }
        } else//deal goes bust
            Possible(win, reward_win, 1.0)
    }

    private fun MDP.hits(s: State) = {
        var player = s[player_idx] + player_offset
        val card = drawCard()
        player += card
        when {
            player <= 21 -> {
                val idx = DefaultIntBuf.from(s)
                idx[player_idx] = player - player_offset
                Possible(states[idx], 0.0, 1.0)
            }
            s[ace_idx] == 0 -> Possible(lose, -1.0, 1.0)
            else -> {
                player -= 10
                val idx = DefaultIntBuf.from(s)
                idx[ace_idx] = 0
                idx[player_idx] = player - player_offset
                Possible(states[idx], 0.0, 1.0)
            }
        }
    }

    private fun drawCard(): Int {
        val index = rand.nextInt(playingCard.size)
        val card = playingCard[index]
        return card
    }
}