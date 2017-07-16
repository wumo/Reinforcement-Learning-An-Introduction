package wumo.rl.model.impl;

import wumo.rl.model.MDP;
import wumo.rl.util.tuple2;

import java.util.Iterator;

public class D2DStateSet implements MDP.StateSet {
    public final MDP.State[][] raw;
    
    public D2DStateSet(int m, int n) {
        raw = new MDP.State[m][n];
    }
    
    @Override public MDP.State get(Object index) {
        tuple2 idx = (tuple2) index;
        return raw[idx._1][idx._2];
    }
    
    @Override public void set(Object index, MDP.State s) {
        tuple2 idx = (tuple2) index;
        raw[idx._1][idx._2] = s;
    }
    
    @Override public Iterator<MDP.State> iterator() {
        return new Iterator<MDP.State>() {
            int a = 0, b = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length && b < raw[a].length;
            }
            
            @Override public MDP.State next() {
                int _a = a, _b = b;
                b++;
                if (b >= raw[a].length) {
                    a++; b = 0;
                }
                return raw[_a][_b];
            }
        };
    }
}