package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;

import java.util.Iterator;

/**
 * single state + D1D reward
 */
public class D1DPossibleSet implements MDP.PossibleSet {
    public final MDP.Possible[] raw;
    
    /**
     * @param reward_size
     *         reward size
     */
    public D1DPossibleSet(int reward_size) {
        this.raw = new MDP.Possible[reward_size];
    }
    
    @Override public MDP.Possible get(Object index) {
        return raw[(int) index];
    }
    
    @Override public void set(Object index, MDP.Possible s) {
        raw[(int) index] = s;
    }
    
    @NotNull @Override public Iterator<MDP.Possible> iterator() {
        return new Iterator<MDP.Possible>() {
            int a = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length;
            }
            
            @Override public MDP.Possible next() {
                int _a = a;
                a++;
                return raw[_a];
            }
        };
    }
}