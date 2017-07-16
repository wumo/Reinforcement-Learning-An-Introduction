package wumo.rl.model;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * <p>
 * Created on 2017-07-13.
 * </p>
 *
 * @author wumo
 */
public class MDP {
    public interface GetterSetter<Idx, E> {
        E get(Idx index);
        
        void set(Idx index, E s);
    }
    
    public interface StateSet extends GetterSetter<Object, State>, Iterable<State> {}
    
    public static class State {
        public ActionSet actions;
    }
    
    public interface ActionSet extends GetterSetter<Object, Action>, Iterable<Action> {
        default Action firstOrNull() {
            Iterator<Action> iter = this.iterator();
            if (iter.hasNext()) return iter.next();
            return null;
        }
    }
    
    public static class Action {
        public final String desc;
        public final int idx;
        
        public Action(int idx, String desc) {
            this.idx = idx;
            this.desc = desc;
        }
        
        public PossibleSet possibles;
        
        @Override public String toString() {
            return desc;
        }
    }
    
    public interface PossibleSet extends GetterSetter<Object, Possible>, Iterable<Possible> {}
    
    public static class Possible {
        public State state;
        public double reward;
        public double probability;
        
        public Possible() {
        }
        
        public Possible(State state, double reward, double probability) {
            this.state = state;
            this.reward = reward;
            this.probability = probability;
        }
    }
    
    public interface StateValueFunction extends GetterSetter<State, Double> {}
    
    public interface ActionValueFunction {
        double get(State s, Action a);
        
        void set(State s, Action a, double value);
    }
    
    public interface Policy extends GetterSetter<State, Action> {}
    
    public final StateSet states;
    public final Supplier<? extends StateValueFunction> V_maker;
    public final Supplier<? extends Policy> pi_maker;
    public final Supplier<? extends ActionValueFunction> Q_maker;
    public final double gamma;
    
    public MDP(StateSet states, double gamma, Supplier<? extends StateValueFunction> v_maker, Supplier<? extends Policy> pi_maker,
               Supplier<? extends ActionValueFunction> q_maker) {
        this.states = states;
        V_maker = v_maker;
        this.pi_maker = pi_maker;
        Q_maker = q_maker;
        this.gamma = gamma;
    }
}
