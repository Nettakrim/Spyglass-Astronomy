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

    public void update(int ticks) {

    }

    public void addLine(int start, int end) {
        for (StarLine line : lines) {
            if (line.isSame(start, end)) return;
        }
        lines.add(new StarLine(start, end));
    }

    public void addLine(StarLine starLine) {
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
