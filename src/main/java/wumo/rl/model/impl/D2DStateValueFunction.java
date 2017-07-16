package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D2DStateValueFunction implements MDP.StateValueFunction {
    public final double[][] raw;
    
    public D2DStateValueFunction(int d2dstate_first_dim, int d2dstate_second_dim) {
        raw = new double[d2dstate_first_dim][d2dstate_second_dim];
    }
    
    @Override public Double get(MDP.State index) {
        D2DState s = (D2DState) index;
        return raw[s._1][s._2];
    }
    
    @Override public void set(MDP.State index, Double v) {
        D2DState s = (D2DState) index;
        raw[s._1][s._2] = v;
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int a = raw.length - 1; a >= 0; a--) {
            for (int b = 0; b < raw[a].length; b++)
                sb.append(String.format("%.2f ", raw[a][b]));
            sb.append("\n");
        }
        return sb.toString();
    }
}