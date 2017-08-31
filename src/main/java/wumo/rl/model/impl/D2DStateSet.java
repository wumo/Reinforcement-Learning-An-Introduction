package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;
import wumo.rl.model.MDP.State;
import wumo.rl.util.index2;

import java.util.Iterator;
import java.util.function.*;

public class D2DStateSet implements MDP.StateSet {
    public final State[][] raw;

    public D2DStateSet(int d2dstate_first_dim, int d2dstate_second_dim) {
        this(d2dstate_first_dim, d2dstate_second_dim, State::new);
    }

    public D2DStateSet(int d2dstate_first_dim, int d2dstate_second_dim, Supplier<? extends State> state_maker) {
        raw = new State[d2dstate_first_dim][d2dstate_second_dim];
        for (int a = 0; a < d2dstate_first_dim; a++)
            for (int b = 0; b < d2dstate_second_dim; b++)
                raw[a][b] = state_maker.get();
    }

    public D2DStateSet(int d2dstate_first_dim, int d2dstate_second_dim, BiFunction<Integer, Integer, ? extends State> state_maker) {
        raw = new State[d2dstate_first_dim][d2dstate_second_dim];
        for (int a = 0; a < d2dstate_first_dim; a++)
            for (int b = 0; b < d2dstate_second_dim; b++)
                raw[a][b] = state_maker.apply(a, b);
    }

    @Override
    public State get(Object index) {
        index2 idx = (index2) index;
        return raw[idx._1][idx._2];
    }

    @Override
    public void set(Object index, State s) {
        index2 idx = (index2) index;
        raw[idx._1][idx._2] = s;
    }

    @NotNull
    @Override
    public Iterator<State> iterator() {
        return new Iterator<State>() {
            int a = 0, b = 0;

            @Override
            public boolean hasNext() {
                return a < raw.length && b < raw[a].length;
            }

            @Override
            public State next() {
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