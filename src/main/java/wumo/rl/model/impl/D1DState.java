package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D1DState extends MDP.State {
    public final int idx;
    
    public D1DState(int idx) {
        this.idx = idx;
    }
    
}