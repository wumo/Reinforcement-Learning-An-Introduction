package wumo.rl.model.impl;

import wumo.rl.model.MDP;

import java.util.Iterator;

public class D1DStateSet implements MDP.StateSet {
    public final MDP.State[] raw;
    
    public D1DStateSet(int n) {
        raw = new MDP.State[n];
    }
    
    @Override public MDP.State get(Object index) {
        return raw[(int) index];
    }
    
    @Override public void set(Object index, MDP.State s) {
        raw[(int) index] = s;
    }
    
    @Override public Iterator<MDP.State> iterator() {
        return new Iterator<MDP.State>() {
            int a = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length;
            }
            
            @Override public MDP.State next() {
                int _a = a;
                a++;
                return raw[_a];
            }
        };
    }
}