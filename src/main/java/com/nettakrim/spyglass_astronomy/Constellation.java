package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;

public class Constellation {
    private ArrayList<StarLine> lines = new ArrayList<>();

    public void Render(BufferBuilder bufferBuilder) {
        for (StarLine line : lines) {
            line.SetVertices(bufferBuilder);
        }
    }

    public void Update(int ticks) {

    }

    public void AddLine(int start, int end) {
        lines.add(new StarLine(start, end));
    }
}
