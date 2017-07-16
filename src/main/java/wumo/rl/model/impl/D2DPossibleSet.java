package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;
import wumo.rl.util.*;

import java.util.Iterator;

/**
 * D1DState + D1D reward
 */
public class D2DPossibleSet implements MDP.PossibleSet {
    public final MDP.Possible[][] raw;
    
    /**
     * @param d1dstate_dim D1DState index
     */
    public D2DPossibleSet(int d1dstate_dim) {
        this.raw = new MDP.Possible[d1dstate_dim][];
    }
    
    @Override public MDP.Possible get(Object index) {
        index2 idx = (index2) index;
        return raw[idx._1][idx._2];
    }
    
    @Override public void set(Object index, MDP.Possible s) {
        index2 idx = (index2) index;
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