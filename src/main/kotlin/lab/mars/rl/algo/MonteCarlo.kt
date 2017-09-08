package lab.mars.rl.algo

import lab.mars.rl.model.MDP

/**
 * <p>
 * Created on 2017-09-07.
 * </p>
 *
 * @author wumo
 */
class MonteCarlo(mdp: MDP) {
    val states = mdp.states
    val gamma = mdp.gamma
    val V = mdp.v_maker()
    val PI = mdp.pi_maker()
    val Q = mdp.q_maker()

    fun iteration() {

    }
}