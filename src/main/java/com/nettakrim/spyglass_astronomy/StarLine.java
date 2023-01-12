package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.MathHelper;

public class StarLine {
    public static float distance = 1.1f;
    public static float width = 0.15f;
    public static float visibilityMultiplier = 0.35f;

    private int starAIndex;
    private int starBIndex;

    private Vec3f starAPosition;
    private Vec3f starBPosition;

    private int[] starAColor;
    private int[] starBColor;

    private Vec3f vertexA1;
    private Vec3f vertexA2;
    private Vec3f vertexB1;
    private Vec3f vertexB2;

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

    public void updateDrawing(Vec3f position) {
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
        Vec3f length = new Vec3f(starAPosition.getX(), starAPosition.getY(), starAPosition.getZ());
        length.subtract(starBPosition);
        return SpyglassAstronomyClient.getSquaredDistance(length.getX(), length.getY(), length.getZ());
    }

    public void calculateVertices() {
        Vec3f direction = new Vec3f(starBPosition.getX(),starBPosition.getY(),starBPosition.getZ());
        direction.subtract(starAPosition);
        float dirX = direction.getX();
        float dirY = direction.getY();
        float dirZ = direction.getZ();
        float sqrDistance = dirX * dirX + dirY * dirY + dirZ * dirZ;
        direction.normalize();
        direction.scale(distance * (Math.min(MathHelper.sqrt(sqrDistance), 4f)/4));

        Vec3f perpendicular = direction.copy();
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.scale(width);

        float posAX = starAPosition.getX() + direction.getX();
        float posAY = starAPosition.getY() + direction.getY();
        float posAZ = starAPosition.getZ() + direction.getZ();

        vertexA1 = new Vec3f(posAX + perpendicular.getX(), posAY + perpendicular.getY(), posAZ + perpendicular.getZ());
        vertexA2 = new Vec3f(posAX - perpendicular.getX(), posAY - perpendicular.getY(), posAZ - perpendicular.getZ());

        perpendicular = new Vec3f(-direction.getX(),-direction.getY(),-direction.getZ());
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.scale(width);

        float posBX = starBPosition.getX() - direction.getX();
        float posBY = starBPosition.getY() - direction.getY();
        float posBZ = starBPosition.getZ() - direction.getZ();

        vertexB1 = new Vec3f(posBX + perpendicular.getX(), posBY + perpendicular.getY(), posBZ + perpendicular.getZ());
        vertexB2 = new Vec3f(posBX - perpendicular.getX(), posBY - perpendicular.getY(), posBZ - perpendicular.getZ());
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
            vertexA1.getX(),
            vertexA1.getY(),
            vertexA1.getZ())
        .color(ar, ag, ab, aa).next();

        bufferBuilder.vertex(
            vertexA2.getX(),
            vertexA2.getY(),
            vertexA2.getZ())
        .color(ar, ag, ab, aa).next();

        bufferBuilder.vertex(
            vertexB1.getX(),
            vertexB1.getY(),
            vertexB1.getZ())
        .color(br, bg, bb, ba).next();

        bufferBuilder.vertex(
            vertexB2.getX(),
            vertexB2.getY(),
            vertexB2.getZ())
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
