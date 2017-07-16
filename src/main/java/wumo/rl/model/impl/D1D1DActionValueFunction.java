package wumo.rl.model.impl;

import wumo.rl.model.MDP;

/**
 * <p>
 * Created on 2017-07-14.
 * </p>
 *
 * @author wumo
 */
public class D1D1DActionValueFunction implements MDP.ActionValueFunction {
    public final double[][] raw;
    
    public D1D1DActionValueFunction(int d1dstate_dim) {
        raw = new double[d1dstate_dim][];
    }
    
    @Override public double get(MDP.State s, MDP.Action a) {
        D1DState _s = (D1DState) s;
        return raw[_s.idx][a.idx];
    }
    
    @Override public void set(MDP.State s, MDP.Action a, double value) {
        D1DState _s = (D1DState) s;
        raw[_s.idx][a.idx] = value;
    }
}
