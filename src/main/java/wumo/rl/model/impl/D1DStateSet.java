package wumo.rl.model.impl;

import wumo.rl.model.MDP;
import wumo.rl.model.MDP.State;

import java.util.Iterator;
import java.util.function.*;

public class D1DStateSet implements MDP.StateSet {
    public final State[] raw;

    public D1DStateSet(int d1dstate_dim) {
        this(d1dstate_dim, State::new);
    }

    public D1DStateSet(int d1dstate_dim, Supplier<? extends State> state_maker) {
        raw = new State[d1dstate_dim];
        for (int a = 0; a < d1dstate_dim; a++)
            raw[a] = state_maker.get();
    }

    public D1DStateSet(int d1dstate_dim, Function<Integer, ? extends State> state_maker) {
        raw = new State[d1dstate_dim];
        for (int a = 0; a < d1dstate_dim; a++)
            raw[a] = state_maker.apply(a);
    }

    @Override
    public State get(Object index) {
        return raw[(int) index];
    }

    @Override
    public void set(Object index, State s) {
        raw[(int) index] = s;
    }

    @Override
    public Iterator<State> iterator() {
        return new Iterator<State>() {
            int a = 0;

            @Override
            public boolean hasNext() {
                return a < raw.length;
            }

            @Override
            public State next() {
                int _a = a;
                a++;
                return raw[_a];
            }
        };
    }
}