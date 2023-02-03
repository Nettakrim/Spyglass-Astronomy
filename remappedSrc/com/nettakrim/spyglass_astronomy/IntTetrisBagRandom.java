package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.util.math.random.Random;

public class IntTetrisBagRandom {
    public ArrayList<Integer> list;
    public final int max;
    private final Random random;

    public IntTetrisBagRandom(Random random, int max) {
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
        int pos = random.nextBetween(0, list.size()-1);
        return list.remove(pos);
    }
}
