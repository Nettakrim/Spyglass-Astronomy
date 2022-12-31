package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;

public class Constellation {
    private ArrayList<StarLine> lines = new ArrayList<>();

    public boolean isActive;

    public Constellation() {

    };

    public Constellation(StarLine starLine) {
        lines.add(starLine);
    }

    public void render(BufferBuilder bufferBuilder) {
        for (StarLine line : lines) {
            line.setVertices(bufferBuilder, isActive);
        }
    }

    public Constellation addLineCanRemove(StarLine starLine) {
        int end = lines.size();
        for (int i = 0; i < end; i++) {
            StarLine line = lines.get(i);
            if (line.isSame(starLine)) {
                line.clear();
                lines.remove(i);
                return trySplit(starLine);
            }
        }
        lines.add(starLine);
        return null;
    }

    public Constellation addLine(StarLine starLine) {
        for (StarLine line : lines) {
            if (line.isSame(starLine)) return null;
        }
        lines.add(starLine);
        return null;
    }

    public Constellation trySplit(StarLine reference) {
        Star[] refStars = reference.getStars();
        int start = refStars[0].index;
        int end = refStars[1].index;

        ArrayList<Integer> found = new ArrayList<Integer>();
        boolean continueSearch = true;

        found.add(start);

        while (continueSearch) {
            continueSearch = false;
            for (StarLine line : lines) {
                for (int index = 0; index < found.size(); index++) {
                    int star = found.get(index);
                    if (line.hasStar(star)) {
                        if (line.hasStar(end)) {
                            return null;
                        }
                        int other = line.getOtherStar(star);
                        if (!found.contains(other)) {
                            found.add(other);
                            continueSearch = true;
                        }
                    }
                }
            }
        }

        ArrayList<Integer> all = new ArrayList<Integer>();
        for (StarLine line : lines) {
            Star[] stars = line.getStars();
            int a = stars[0].index;
            int b = stars[1].index;
            if (!all.contains(a)) {
                all.add(a);
            }
            if (!all.contains(b)) {
                all.add(b);
            }
        }

        if (found.size() != all.size()) {
            Constellation newConstellation = new Constellation();
            for (StarLine line : lines) {
                boolean isFound = false;
                for (int star : found) {
                    if (line.hasStar(star)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    newConstellation.addLine(line);
                }
            }
            for (StarLine newLine : newConstellation.lines) {
                int index = 0;
                while (index < lines.size()) {
                    if (lines.get(index).isSame(newLine)) {
                        lines.remove(index);
                    } else {
                        index++;
                    }
                }
            }
            return newConstellation;
        }

        return null;
    }

    public boolean lineIntersects(StarLine starLine) {
        for (StarLine line : lines) {
            if (line.intersects(starLine)) return true;
        }
        return false;
    }

    public boolean hasStar(Star star) {
        int index = star.index;
        for (StarLine line : lines) {
            if (line.hasStar(index)) return true;
        }
        return false;        
    }

    public ArrayList<StarLine> getLines() {
        return lines;
    }

    public void initaliseStarLines() {
        for (StarLine line : lines) {
            line.initialise();
        }
    }
}
