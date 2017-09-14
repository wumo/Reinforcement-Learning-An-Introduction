package lab.mars.rl.problem

import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.Possible
import lab.mars.rl.model.State
import lab.mars.rl.model.impl.Dim
import lab.mars.rl.model.impl.NSet
import lab.mars.rl.model.impl.NSetMDP
import lab.mars.rl.model.impl.x
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.FastMath.abs
import java.util.*

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */

fun Int.sign() = if (this > 0) 1 else if (this < 0) -1 else 0

object Blackjack {
    private val playingCard = intArrayOf(1/*A*/, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10/*J*/, 10/*Q*/, 10/*K*/)
    private const val player_offset = 12
    private const val dealer_offset = 1

    private const val win = 1
    private const val draw = 0
    private const val lose = -1

    private const val usableAce_idx = 1
    private const val playerSum_idx = 2
    private const val dealShownCard_idx = 3
    private val rand = Random(System.nanoTime())
    fun make(): MDP {
        val mdp = NSetMDP(gamma = 1.0, state_dim = 2 x 10 x 10, action_dim = 2)
        mdp.apply {
            for (s in states)
                for (action in s.actions)
                    when (action.idx[0]) {
                        0 -> sticks(action, s)
                        1 -> hits(action, s)
                    }
        }
        return mdp
    }

    private fun MDP.sticks(action: Action, s: State) {
        action.sample = {
            var dealer = s.idx[dealShownCard_idx] + dealer_offset
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
                val player = s.idx[playerSum_idx] + player_offset
                val sign = (player - dealer).sign()
                Possible(states[0, sign], sign.toDouble(), 1.0)
            } else//deal goes bust
                Possible(states[0, win], win.toDouble(), 1.0)
        }
    }

    private fun MDP.hits(action: Action, s: State) {
        action.sample = {
            var player = s.idx[playerSum_idx] + player_offset
            val card = drawCard()
            player += card
            when {
                player <= 21 -> {
                    val idx = s.idx.copyOf()
                    idx[playerSum_idx] = player - player_offset
                    Possible(states.get(*idx), 0.0, 1.0)
                }
                s.idx[usableAce_idx] == 0 -> Possible(states[0, lose], lose.toDouble(), 1.0)
                else -> {
                    player -= 10
                    val idx = s.idx.copyOf()
                    idx[usableAce_idx] = 0
                    idx[playerSum_idx] = player - player_offset
                    Possible(states.get(*idx), 0.0, 1.0)
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