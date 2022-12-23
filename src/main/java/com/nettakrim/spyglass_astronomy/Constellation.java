package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;

public class Constellation {
    private ArrayList<StarLine> lines = new ArrayList<>();

    public Constellation() {

    };

    public Constellation(StarLine starLine) {
        lines.add(starLine);
    }

    public void render(BufferBuilder bufferBuilder) {
        for (StarLine line : lines) {
            line.setVertices(bufferBuilder);
        }
    }

    public void addLine(StarLine starLine, boolean canRemove) {
        if (canRemove) {
            int end = lines.size();
            for (int i = 0; i < end; i++) {
                StarLine line = lines.get(i);
                if (line.isSame(starLine)) {
                    line.clear();
                    lines.remove(i);
                    return;
                }
            }
            lines.add(starLine);
            return;
        }

        for (StarLine line : lines) {
            if (line.isSame(starLine)) return;
        }
        lines.add(starLine);
    }

    public boolean lineIntersects(StarLine starLine) {
        for (StarLine line : lines) {
            if (line.intersects(starLine)) return true;
        }
        return false;
    }
}
