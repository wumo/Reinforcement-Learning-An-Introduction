package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;
import wumo.rl.util.index3;

import java.util.Iterator;

/**
 * D2DState + D1D reward
 */
public class D3DPossibleSet implements MDP.PossibleSet {
    public final MDP.Possible[][][] raw;
    
    /**
     * @param d2dstate_first_dim
     *         D2DState's first index
     * @param d2dstate_second_dim
     *         D2DState's second index
     */
    public D3DPossibleSet(int d2dstate_first_dim, int d2dstate_second_dim) {
        this.raw = new MDP.Possible[d2dstate_first_dim][d2dstate_second_dim][];
    }
    
    @Override public MDP.Possible get(Object index) {
        index3 idx = (index3) index;
        return raw[idx._1][idx._2][idx._3];
    }
    
    @Override public void set(Object index, MDP.Possible s) {
        index3 idx = (index3) index;
        raw[idx._1][idx._2][idx._3] = s;
    }
    
    @NotNull @Override public Iterator<MDP.Possible> iterator() {
        return new Iterator<MDP.Possible>() {
            int a = 0, b = 0, c = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length && b < raw[a].length && c < raw[a][b].length;
            }
            
            @Override public MDP.Possible next() {
                int _a = a, _b = b, _c = c;
                c++;
                if (c >= raw[a][b].length) {
                    b++; c = 0;
                    if (b >= raw[a].length) {
                        a++; b = 0;
                    }
                }
                return raw[_a][_b][_c];
            }
        };
    }
}