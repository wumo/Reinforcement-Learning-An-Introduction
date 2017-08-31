package wumo.rl.model.impl;

import wumo.rl.model.MDP;

/**
 * <p>
 * Created on 2017-08-31.
 * </p>
 * @author wumo
 */
public class DnDState extends MDP.State{
    public final int[] idx;

    public DnDState(int... idx) {
        this.idx = idx;
    }
}
