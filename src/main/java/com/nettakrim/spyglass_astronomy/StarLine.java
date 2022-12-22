package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.Vec3f;

public class StarLine {
    public static float distance = 1.1f;
    public static float width = 0.15f;
    public static float visibilityMultiplier = 0.5f;

    private final int starAIndex;
    private final int starBIndex;

    private final Vec3f starAPosition;
    private final Vec3f starBPosition;

    private final int[] starAColor;
    private final int[] starBColor;

    public StarLine(int startIndex, int endIndex) {
        this.starAIndex = startIndex;
        this.starBIndex = endIndex;

        Star starA = SpyglassAstronomyClient.stars.get(startIndex);
        this.starAPosition = starA.getRenderedPosition();
        this.starAColor = starA.getColor();

        Star starB = SpyglassAstronomyClient.stars.get(endIndex);
        this.starBPosition = starB.getRenderedPosition();
        this.starBColor = starB.getColor();
    }

    public void SetVertices(BufferBuilder bufferBuilder) {
        //direction A needs to go to reach B
        Vec3f direction = new Vec3f(starBPosition.getX(),starBPosition.getY(),starBPosition.getZ());
        direction.subtract(starAPosition);
        direction.normalize();
        direction.scale(distance);

        Vec3f perpendicular = new Vec3f(direction.getX(),direction.getY(),direction.getZ());
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.scale(width);

        float posAX = starAPosition.getX() + direction.getX();
        float posAY = starAPosition.getY() + direction.getY();
        float posAZ = starAPosition.getZ() + direction.getZ();

        bufferBuilder.vertex(
            posAX + perpendicular.getX(),
            posAY + perpendicular.getY(),
            posAZ + perpendicular.getZ())
        .color(starAColor[0], starAColor[1], starAColor[2], (int)(starAColor[3] * visibilityMultiplier)).next();

        bufferBuilder.vertex(
            posAX - perpendicular.getX(),
            posAY - perpendicular.getY(),
            posAZ - perpendicular.getZ())
        .color(starAColor[0], starAColor[1], starAColor[2], (int)(starAColor[3] * visibilityMultiplier)).next();

        perpendicular = new Vec3f(-direction.getX(),-direction.getY(),-direction.getZ());
        perpendicular.cross(starAPosition);
        perpendicular.normalize();
        perpendicular.scale(width);

        float posBX = starBPosition.getX() - direction.getX();
        float posBY = starBPosition.getY() - direction.getY();
        float posBZ = starBPosition.getZ() - direction.getZ();

        bufferBuilder.vertex(
            posBX + perpendicular.getX(),
            posBY + perpendicular.getY(),
            posBZ + perpendicular.getZ())
        .color(starBColor[0], starBColor[1], starBColor[2], (int)(starBColor[3] * visibilityMultiplier)).next();

        bufferBuilder.vertex(
            posBX - perpendicular.getX(),
            posBY - perpendicular.getY(),
            posBZ - perpendicular.getZ())
        .color(starBColor[0], starBColor[1], starBColor[2], (int)(starBColor[3] * visibilityMultiplier)).next();
    }

    public Star[] getStars() {
        return new Star[]{SpyglassAstronomyClient.stars.get(starAIndex),SpyglassAstronomyClient.stars.get(starBIndex)};
    }
}
