package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D1DPolicy implements MDP.Policy {
    public final MDP.Action[] raw;
    
    public D1DPolicy(int n) {
        raw = new MDP.Action[n];
    }
    
    @Override public MDP.Action get(MDP.State index) {
        D1DState s = (D1DState) index;
        return raw[s.idx];
    }
    
    @Override public void set(MDP.State index, MDP.Action a) {
        D1DState s = (D1DState) index;
        raw[s.idx] = a;
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < raw.length; a++)
            sb.append(raw[a]);
        sb.append("\n");
        return sb.toString();
    }
}