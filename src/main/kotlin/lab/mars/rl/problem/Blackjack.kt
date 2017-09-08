package lab.mars.rl.problem

import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.Possible
import lab.mars.rl.model.impl.NSet
import lab.mars.rl.model.impl.NSetMDP
import java.util.*

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
object Blackjack {
    val playingCard = intArrayOf(1/*A*/, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10/*J*/, 10/*Q*/, 10/*K*/)
    const val player_offset = 12
    const val dealer_offset = 1
    const val usableAce_idx = 0
    const val playerSum_idx = 1
    const val dealShownCard_idx = 2
    val rand = Random(System.nanoTime())
    fun make(): MDP {
        val mdp = NSetMDP(gamma = 1.0, state_dim = intArrayOf(2, 10, 10), action_dim = intArrayOf(2))
        mdp.apply {
            for (s in states) {
                s!!.actions = NSet(2) { action_idx ->
                    val action = Action(action_idx)
                    when (action_idx[0]) {
                        0 -> {//sticks
//                            action.sample = {
//                                var dealer = s.idx[dealShownCard_idx] + dealer_offset
//                                var usableAceDealer = dealer == 1
//                                if (usableAceDealer)
//                                    dealer += 10
//                                while (dealer < 17) {
//                                    val card = drawCard()
//                                    dealer += card
//                                    if (21 < dealer && usableAceDealer) {
//                                        dealer -= 10
//                                        usableAceDealer = false
//                                    }
//                                }
////                                if (dealer <= 21) {
////                                    val player = s.idx[playerSum_idx] + player_offset
////                                    Possible(player-dealer)
////                                }
//                                Possible()
//                            }
                        }
                        1 -> {//hits
//                            action.sample = {
//                                var player = s.idx[playerSum_idx] + player_offset
//                                val card = drawCard()
//                                player += card
////                                if (player <= 21) {
////                                    val idx = s.idx.copyOf()
////                                    idx[playerSum_idx] = player - player_offset
////                                    Possible(states.get(*idx)!!, 0.0, 1.0)
////                                } else if(){
////
////                                }
//                            }
                        }
                    }
                    action
                }
            }
        }
        return mdp
    }

    private fun drawCard(): Int {
        val index = rand.nextInt(playingCard.size)
        val card = playingCard[index]
        return card
    }
}