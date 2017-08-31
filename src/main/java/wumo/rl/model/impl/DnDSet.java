package wumo.rl.model.impl;

import org.jetbrains.annotations.NotNull;
import wumo.rl.model.MDP;
import wumo.rl.model.MDP.State;

import java.util.*;
import java.util.function.*;

public class DnDSet<Idx extends DnDIndex, E> implements MDP.GetterSetter<Idx, E>, Iterable<E> {
    public final Object[] raw;
    private final int[] dim, stride;

    public DnDSet(Supplier<? extends State> state_maker, int... dim) {
        this.dim = dim;
        this.stride = new int[dim.length];
        stride[stride.length - 1] = 1;
        for (int a = stride.length - 2; a >= 0; a--)
            stride[a] = dim[a + 1] * stride[a + 1];
        int total = dim[0] * stride[0];
        raw = new Object[total];
        for (int a = 0; a < total; a++)
            raw[a] = state_maker.get();
    }

    private int offset(int... idx) {
        int offset = 0;
        if (idx.length != dim.length)
            throw new RuntimeException("index.length=" + idx.length + " > dim.length=" + dim.length);
        for (int a = 0; a < idx.length; a++) {
            if (idx[a] < 0 || idx[a] > dim[a])
                throw new ArrayIndexOutOfBoundsException("index[" + a + "]=" + idx[a] + " while dim[" + a + "]=" + dim[a]);
            offset += idx[a] * stride[a];
        }
        return offset;
    }

    public E get(Idx index) {
        return (E) raw[offset(index.idx())];
    }


    public void set(Idx index, E s) {
        raw[offset(index.idx())] = s;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int a = 0;

            @Override
            public boolean hasNext() {
                return a < raw.length;
            }

            @Override
            public E next() {
                int _a = a;
                a++;

                return (E) raw[_a];
            }
        };
    }
}