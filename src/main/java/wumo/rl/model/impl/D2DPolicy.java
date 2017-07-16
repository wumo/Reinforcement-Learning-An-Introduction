package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D2DPolicy implements MDP.Policy {
    public final MDP.Action[][] raw;
    
    public D2DPolicy(int m, int n) {
        raw = new MDP.Action[m][n];
    }
    
    @Override public MDP.Action get(MDP.State index) {
        D2DState s = (D2DState) index;
        return raw[s._1][s._2];
    }
    
    @Override public void set(MDP.State index, MDP.Action a) {
        D2DState s = (D2DState) index;
        raw[s._1][s._2] = a;
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < raw.length; a++) {
            for (int b = 0; b < raw[a].length; b++)
                sb.append(raw[a][b]);
            sb.append("\n");
        }
        return sb.toString();
    }
}