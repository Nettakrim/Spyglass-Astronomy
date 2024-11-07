package com.nettakrim.spyglass_astronomy;

import org.joml.Vector3f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;

public class StarLine {
    public static float distance = 1.1f;
    public static float width = 0.15f;
    public static float visibilityMultiplier = 0.35f;

    private int starAIndex;
    private int starBIndex;

    private Vector3f starAPosition;
    private Vector3f starBPosition;

    private int[] starAColor;
    private int[] starBColor;

    private Vector3f vertexA1;
    private Vector3f vertexA2;
    private Vector3f vertexB1;
    private Vector3f vertexB2;

    public StarLine(int startIndex, int endIndex, boolean starsReady) {
        this.starAIndex = startIndex;
        this.starBIndex = endIndex;

        if (starsReady) {
            initialise();
        }
    }

    public void initialise() {
        Star starA = SpyglassAstronomyClient.stars.get(this.starAIndex);
        this.starAPosition = starA.getRenderedPosition();
        this.starAColor = starA.getColor();
        starA.connect();

        Star starB = SpyglassAstronomyClient.stars.get(this.starBIndex);
        this.starBPosition = starB.getRenderedPosition();
        this.starBColor = starB.getColor();
        starB.connect();

        calculateVertices();        
    }

    public StarLine(Star startStar) {
        this.starAIndex = startStar.index;
        this.starBIndex = -1;

        this.starAPosition = startStar.getRenderedPosition();
        this.starAColor = startStar.getColor();

        this.starBPosition = startStar.getRenderedPosition();
        this.starBColor = new int[]{255,255,255,255};
    }

    public void updateDrawing(Vector3f position) {
        this.starBPosition = position;
        calculateVertices();
    }

    public boolean finishDrawing(Star endStar) {
        if (this.starAIndex == endStar.index) return false;
        if (getSquaredLength() > 20000) return false;
        this.starBIndex = endStar.index;
        this.starBPosition = endStar.getRenderedPosition();
        this.starBColor = endStar.getColor();

        endStar.connect();
        SpyglassAstronomyClient.stars.get(starAIndex).connect();
        calculateVertices();     
        return true;
    }

    public float getSquaredLength() {
        Vector3f length = new Vector3f(starAPosition.x, starAPosition.y, starAPosition.z);
        length.sub(starBPosition);
        return SpyglassAstronomyClient.getSquaredDistance(length.x, length.y, length.z);
    }

    public void calculateVertices() {
        Vector3f direction = new Vector3f(starBPosition.x,starBPosition.y,starBPosition.z);
        direction.sub(starAPosition);
        float dirX = direction.x;
        float dirY = direction.y;
        float dirZ = direction.z;
        float sqrDistance = dirX * dirX + dirY * dirY + dirZ * dirZ;
        direction.normalize();
        direction.mul(distance * (Math.min(MathHelper.sqrt(sqrDistance), 4f)/4));

        Vector3f perpendicular = new Vector3f(direction);
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.mul(width);

        float posAX = starAPosition.x + direction.x;
        float posAY = starAPosition.y + direction.y;
        float posAZ = starAPosition.z + direction.z;

        vertexA1 = new Vector3f(posAX + perpendicular.x, posAY + perpendicular.y, posAZ + perpendicular.z);
        vertexA2 = new Vector3f(posAX - perpendicular.x, posAY - perpendicular.y, posAZ - perpendicular.z);

        perpendicular = new Vector3f(-direction.x,-direction.y,-direction.z);
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.mul(width);

        float posBX = starBPosition.x - direction.x;
        float posBY = starBPosition.y - direction.y;
        float posBZ = starBPosition.z - direction.z;

        vertexB1 = new Vector3f(posBX + perpendicular.x, posBY + perpendicular.y, posBZ + perpendicular.z);
        vertexB2 = new Vector3f(posBX - perpendicular.x, posBY - perpendicular.y, posBZ - perpendicular.z);
    }

    public void setVertices(BufferBuilder bufferBuilder, boolean isSelected) {
        if (vertexA1 == null) calculateVertices();
        float drawingMultipler = 1;
        if (starBIndex == -1) {
            drawingMultipler = MathHelper.clamp((20000f-getSquaredLength())/5000f, 0f, 1f);
        }
        
        int ar = starAColor[0];
        int br = starBColor[0];

        int bg = starAColor[1];
        int ag = starBColor[1];

        int bb = starAColor[2];
        int ab = starBColor[2];

        int aa = (int)(starAColor[3] * visibilityMultiplier * drawingMultipler);
        int ba = (int)(starBColor[3] * visibilityMultiplier * drawingMultipler);

        if (isSelected) {
            ar = (int)(ag*0.8f);
            br = (int)(bg*0.8f);
            ag = (int)(ag*0.5f);
            bg = (int)(bg*0.5f);
            ab = (int)Math.min(ab*1.5f, 255f);
            bb = (int)Math.min(bb*1.5f, 255f);

            aa = aa*2;
            ba = ba*2;
        }

        bufferBuilder.vertex(
            vertexA1.x,
            vertexA1.y,
            vertexA1.z)
        .color(ar, ag, ab, aa).next();

        bufferBuilder.vertex(
            vertexA2.x,
            vertexA2.y,
            vertexA2.z)
        .color(ar, ag, ab, aa).next();

        bufferBuilder.vertex(
            vertexB1.x,
            vertexB1.y,
            vertexB1.z)
        .color(br, bg, bb, ba).next();

        bufferBuilder.vertex(
            vertexB2.x,
            vertexB2.y,
            vertexB2.z)
        .color(br, bg, bb, ba).next();
    }

    public void clear() {
        SpyglassAstronomyClient.stars.get(starAIndex).disconnect();
        SpyglassAstronomyClient.stars.get(starBIndex).disconnect();
    }

    public Star[] getStars() {
        return new Star[]{SpyglassAstronomyClient.stars.get(starAIndex), SpyglassAstronomyClient.stars.get(starBIndex)};
    }

    public boolean isSame(int a, int b) {
        return (a == this.starAIndex && b == this.starBIndex) || (a == this.starBIndex && b == this.starAIndex);
    }

    public boolean isSame(StarLine other) {
        return isSame(other.starAIndex, other.starBIndex);
    }

    public boolean intersects(int a, int b) {
        return a == this.starAIndex || b == this.starBIndex || a == this.starBIndex || b == this.starAIndex;
    }

    public boolean intersects(StarLine other) {
        return intersects(other.starAIndex, other.starBIndex);
    }

    public boolean hasStar(int star) {
        return this.starAIndex == star || this.starBIndex == star;
    }

    public int getOtherStar(int star) {
        return this.starAIndex == star ? this.starBIndex : this.starAIndex;
    }
}
