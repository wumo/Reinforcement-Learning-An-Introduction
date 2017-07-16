package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;

import java.util.Iterator;

public class D1DActionSet implements MDP.ActionSet {
    public final MDP.Action[] raw;
    
    public D1DActionSet(int n) {
        this.raw = new MDP.Action[n];
    }
    
    @Override public MDP.Action get(Object index) {
        return raw[(int) index];
    }
    
    @Override public void set(Object index, MDP.Action s) {
        raw[(int) index] = s;
    }
    
    @NotNull @Override public Iterator<MDP.Action> iterator() {
        return new Iterator<MDP.Action>() {
            int a = 0;
            
            @Override public boolean hasNext() {
                return a < raw.length;
            }
            
            @Override public MDP.Action next() {
                int _a = a;
                a++;
                return raw[_a];
            }
        };
    }
}