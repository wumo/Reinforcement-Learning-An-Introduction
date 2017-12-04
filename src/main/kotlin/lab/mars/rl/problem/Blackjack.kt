package lab.mars.rl.problem

import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.collection.emptyNSet
import lab.mars.rl.util.dimension.invoke
import lab.mars.rl.util.dimension.x
import lab.mars.rl.util.math.Rand

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */

object Blackjack {
  private val playingCard = intArrayOf(1/*A*/, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10/*J*/, 10/*Q*/, 10/*K*/)
  private lateinit var win: IndexedState
  private lateinit var draw: IndexedState
  private lateinit var lose: IndexedState

  private const val reward_win = 1.0
  private const val reward_draw = 0.0
  private const val reward_lose = -1.0

  private const val ace_idx = 1
  private const val dealer_idx = 2
  private const val player_idx = 3
  private const val player_offset = 12
  private const val dealer_offset = 1

  fun make(): IndexedProblem {
    val mdp = CNSetMDP(gamma = 1.0, state_dim = 0(3, 2 x 10 x 10), action_dim = { if (it[0] == 0) 1 else 2 })
    mdp.apply {
      win = states[0, 0]
      draw = states[0, 1]
      lose = states[0, 2]
      win.actions = emptyNSet()
      draw.actions = emptyNSet()
      lose.actions = emptyNSet()
      started = { states(1).rand() }
      for (s in states)
        for (a in s.actions)
          when (a[0]) {
            0 -> a.sample = sticks(s)
            1 -> a.sample = hits(s)
          }
    }
    val policy1 = mdp.QFunc { 0.0 }
    for (s in mdp.states(1))
      if (s[player_idx] >= 20 - player_offset)
        policy1[s, s.actions[0]] = 1.0
      else
        policy1[s, s.actions[1]] = 1.0
    return Pair(mdp, IndexedPolicy(policy1))
  }

  private fun IndexedMDP.sticks(s: IndexedState) = {
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
        player > dealer -> IndexedPossible(win, reward_win, 1.0)
        player == dealer -> IndexedPossible(draw, reward_draw, 1.0)
        player < dealer -> IndexedPossible(lose, reward_lose, 1.0)
        else -> throw Exception("impossible")
      }
    } else//deal goes bust
      IndexedPossible(win, reward_win, 1.0)
  }

  private fun IndexedMDP.hits(s: IndexedState) = {
    var player = s[player_idx] + player_offset
    val card = drawCard()
    player += card
    when {
      player <= 21 -> {
        val idx = DefaultIntBuf.from(s)
        idx[player_idx] = player - player_offset
        IndexedPossible(states[idx], 0.0, 1.0)
      }
      s[ace_idx] == 0 -> IndexedPossible(lose, -1.0, 1.0)
      else -> {
        player -= 10
        val idx = DefaultIntBuf.from(s)
        idx[ace_idx] = 0
        idx[player_idx] = player - player_offset
        IndexedPossible(states[idx], 0.0, 1.0)
      }
    }
  }

  private fun drawCard(): Int {
    val index = Rand().nextInt(playingCard.size)
    val card = playingCard[index]
    return card
  }
}