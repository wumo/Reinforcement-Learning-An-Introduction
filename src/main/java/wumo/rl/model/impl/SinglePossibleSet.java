package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;

import java.util.Iterator;

/**
 * <p>
 * Created on 2017-07-13.
 * </p>
 *
 * @author wumo
 */
public class SinglePossibleSet implements MDP.PossibleSet {
    public MDP.Possible raw;
    
    public SinglePossibleSet(MDP.Possible raw) {
        this.raw = raw;
    }
    
    @NotNull @Override public Iterator<MDP.Possible> iterator() {
        return new Iterator<MDP.Possible>() {
            boolean visited = false;
            
            @Override public boolean hasNext() {
                return !visited;
            }
            
            @Override public MDP.Possible next() {
                visited = true;
                return raw;
            }
        };
    }
    
    @Override public MDP.Possible get(Object index) {
        return raw;
    }
    
    @Override public void set(Object index, MDP.Possible s) {
        raw = s;
    }
}
