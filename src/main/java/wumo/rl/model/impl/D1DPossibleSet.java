package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;
import wumo.rl.util.tuple2;

import java.util.Iterator;

/**
 * single state + D1D reward
 */
public class D1DPossibleSet implements MDP.PossibleSet {
    public final MDP.Possible[][] raw;
    
    public D1DPossibleSet(int n) {
        this.raw = new MDP.Possible[n][];
    }
    
    @Override public MDP.Possible get(Object index) {
        tuple2 idx = (tuple2) index;
        return raw[idx._1][idx._2];
    }
    
    @Override public void set(Object index, MDP.Possible s) {
        tuple2 idx = (tuple2) index;
        raw[idx._1][idx._2] = s;
    }
    
    @NotNull @Override public Iterator<MDP.Possible> iterator() {
        return new Iterator<MDP.Possible>() {
            int a = 0, b = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length && b < raw[a].length;
            }
            
            @Override public MDP.Possible next() {
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