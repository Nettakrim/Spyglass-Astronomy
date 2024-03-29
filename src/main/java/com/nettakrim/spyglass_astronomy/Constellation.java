package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import org.joml.Vector3f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;

public class Constellation {
    private final ArrayList<StarLine> lines = new ArrayList<>();

    public String name = "Unnamed";

    private Vector3f averagePositionBuffer;
    private boolean averagePositionValid;

    public static Constellation selected;
    private boolean isSelected;

    public Constellation() {

    }

    public Constellation(StarLine starLine) {
        lines.add(starLine);
    }

    public void setVertices(BufferBuilder bufferBuilder, boolean forceSelected) {
        for (StarLine line : lines) {
            line.setVertices(bufferBuilder, isSelected || forceSelected);
        }
    }

    public Constellation addLineCanRemove(StarLine starLine) {
        averagePositionValid = false;
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

    public void addLine(StarLine starLine) {
        for (StarLine line : lines) {
            if (line.isSame(starLine)) return;
        }
        lines.add(starLine);
        averagePositionValid = false;
    }

    public Constellation trySplit(StarLine reference) {
        Star[] refStars = reference.getStars();
        int start = refStars[0].index;
        int end = refStars[1].index;

        ArrayList<Integer> found = new ArrayList<>();
        boolean continueSearch = true;

        found.add(start);

        //flood outwards from one end, stop if other end found (as in a loop exists), otherwise log all found
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

        //get all stars in the original constellation
        ArrayList<Integer> all = new ArrayList<>();
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

        //if the amount of stars found is not the entire constellation (i dont remember what purpose this serves >_>)
        if (found.size() != all.size()) {
            //add all lines that were not found to the new constellation
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

            //i wouldve thought this is equivalent to the if statement this is within, but apparently not
            if (newConstellation.lines.size() == lines.size()) {
                return null;
            }

            //remove the lines of the old constellation from the new one
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
            newConstellation.name = name;
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

    public boolean hasNoMatchingLine(StarLine starLine) {
        for (StarLine line : lines) {
            if (line.isSame(starLine)) return false;
        }
        return true;
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

    public Vector3f getAveragePosition() {
        if (averagePositionValid) return averagePositionBuffer;

        averagePositionBuffer = new Vector3f();
        ArrayList<Star> stars = new ArrayList<>();
        for (StarLine line : lines) {
            Star[] lineStars = line.getStars();
            if (!stars.contains(lineStars[0])) stars.add(lineStars[0]);
            if (!stars.contains(lineStars[1])) stars.add(lineStars[1]);
        }
        for (Star star : stars) {
            averagePositionBuffer.add(star.getPositionAsVector3f());
        }
        float x = averagePositionBuffer.x;
        float y = averagePositionBuffer.y;
        float z = averagePositionBuffer.z;
        float isqrt = MathHelper.inverseSqrt(x * x + y * y + z * z);
        averagePositionBuffer.mul(isqrt);
        averagePositionValid = true;

        return new Vector3f(averagePositionBuffer);
    }

    public void select() {
        Star.deselect();
        OrbitingBody.deselect();
        if (selected != null) selected.isSelected = false;
        isSelected = true;
        selected = this;
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static void deselect() {
        if (selected != null) selected.isSelected = false;
        selected = null;
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public boolean isUnnamed() {
        return name.equals("Unnamed");
    }
}
