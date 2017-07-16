package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;

import java.util.Iterator;

/**
 * <p>
 * Created on 2017-07-16.
 * </p>
 *
 * @author wumo
 */
public class EmptyActionSet implements MDP.ActionSet {
    public static final EmptyActionSet instance=new EmptyActionSet();
    private static final Iterator<MDP.Action> iter = new Iterator<MDP.Action>() {
        @Override public boolean hasNext() {
            return false;
        }
        
        @Override public MDP.Action next() {
            return null;
        }
    };
    
    @NotNull @Override public Iterator<MDP.Action> iterator() {
        return iter;
    }
    
    @Override public MDP.Action get(Object index) {
        return null;
    }
    
    @Override public void set(Object index, MDP.Action s) {
    }
}
