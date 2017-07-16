package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D1DStateValueFunction implements MDP.StateValueFunction {
    public final double[] raw;
    
    public D1DStateValueFunction(int d1dstate_dim) {
        raw = new double[d1dstate_dim];
    }
    
    @Override public Double get(MDP.State index) {
        D1DState s = (D1DState) index;
        return raw[s.idx];
    }
    
    @Override public void set(MDP.State index, Double v) {
        D1DState s = (D1DState) index;
        raw[s.idx] = v;
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < raw.length; a--)
            sb.append(String.format("%.2f ", raw[a]));
        sb.append("\n");
        return sb.toString();
    }
}