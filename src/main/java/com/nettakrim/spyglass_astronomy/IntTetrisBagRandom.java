package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.util.RandomSource;

public class IntTetrisBagRandom {
    public ArrayList<Integer> list;
    public final int max;
    private final RandomSource random;

    public IntTetrisBagRandom(RandomSource random, int max) {
        this.random = random;
        this.max = max;
        reset();
    }

    public void reset() {
        list = new ArrayList<>();
        for (int x = 0; x < max+1; x++) {
            list.add(x);
        }
    }

    public int get() {
        if (list.size() == 0) reset();
        int pos = random.nextIntBetweenInclusive(0, list.size()-1);
        return list.remove(pos);
    }
}
