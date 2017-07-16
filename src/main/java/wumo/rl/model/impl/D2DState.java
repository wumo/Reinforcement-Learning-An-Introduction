package wumo.rl.model.impl;

import wumo.rl.model.MDP;

public class D2DState extends MDP.State {
    public final int _1, _2;
    
    public D2DState(int _1, int _2) {
        this._1 = _1;
        this._2 = _2;
    }
    
}