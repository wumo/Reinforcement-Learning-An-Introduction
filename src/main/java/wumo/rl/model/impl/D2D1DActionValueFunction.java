package wumo.rl.model.impl;

import wumo.rl.model.MDP;

/**
 * <p>
 * Created on 2017-07-14.
 * </p>
 *
 * @author wumo
 */
public class D2D1DActionValueFunction implements MDP.ActionValueFunction {
    public final double[][][] raw;
    
    public D2D1DActionValueFunction(int m, int n) {
        raw = new double[m][n][];
    }
    
    @Override public double get(MDP.State s, MDP.Action a) {
        D2DState _s = (D2DState) s;
        return raw[_s._1][_s._2][a.idx];
    }
    
    @Override public void set(MDP.State s, MDP.Action a, double value) {
        D2DState _s = (D2DState) s;
        raw[_s._1][_s._2][a.idx] = value;
    }
}
