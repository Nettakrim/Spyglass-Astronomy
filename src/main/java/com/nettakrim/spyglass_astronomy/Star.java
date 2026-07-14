package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.util.Mth;

//https://github.com/ZtereoHYPE/nicer-skies/blob/main/src/main/java/codes/ztereohype/nicerskies/sky/star/Star.java

public class Star {
    public final int index;

    private final float xCoord;
    private final float yCoord;
    private final float zCoord;

    private final int r;
    private final int g;
    private final int b;
    private final float alpha;

    private final float angle;
    private final float size;
    private final int seed;

    private int currentAlpha;

    private int connectedLines = 0;

    public static Star selected;
    private boolean isSelected;

    public String name;

    public Star(int index, float posX, float posY, float posZ, float size, int[] color, float alpha, float rotationSeed, float twinkleSeed) {
        this.index = index;
 
        this.xCoord = posX;
        this.yCoord = posY;
        this.zCoord = posZ;
 
        this.r = color[0];
        this.g = color[1];
        this.b = color[2];
        this.alpha = alpha;

        float normalisedRotation = (rotationSeed * 2f)-1;
        this.angle = normalisedRotation * Mth.PI;
        this.size = size;

        this.seed = Mth.floor(rotationSeed*15) + Mth.floor(twinkleSeed*15)*16;
    }

    public float calculateAlpha() {
        float heightScale = SpaceRenderingManager.getHeightScale();
        float brightness = heightScale*Math.max(alpha/2 + heightScale/2, 2*alpha-1) + (1-heightScale) * alpha * alpha * alpha;
        if (connectedLines != 0) {
            brightness = (brightness + (0.5f * alpha + 0.5f))/2;
        }
        currentAlpha = (int)(brightness * 255);
        return brightness;
    }

    public void setVertices(BufferBuilder bufferBuilder, TextureAtlasSprite sprite) {
        int colorMultiplier = isSelected ? 1 : 0;

        if (sprite != null) {
            // cosmos
            float angleSin = Mth.sin(angle);
            float angleCos = Mth.cos(angle);

            double polarAngle = Math.atan2(xCoord, zCoord);
            float longitudeSin = (float) Math.sin(polarAngle);
            float longitudeCos = (float) Math.cos(polarAngle);

            double proj = Math.atan2(Math.sqrt(xCoord * xCoord + zCoord * zCoord), yCoord);
            float latitudeSin = (float) Math.sin(proj);
            float latitudeCos = (float) Math.cos(proj);

            calculateAlpha();

            for (int corner = 0; corner < 4; ++corner) {
                float x = ((corner & 2) - 1) * size;
                float y = ((corner + 1 & 2) - 1) * size;
                float rotatedA = x * angleCos - y * angleSin;
                float rotatedB = y * angleCos + x * angleSin;
                float rotatedALat = rotatedA * latitudeSin;
                float rotatedBLat = -(rotatedA * latitudeCos);
                float vertexPosX = rotatedBLat * longitudeSin - rotatedB * longitudeCos;
                float vertexPosZ = rotatedB * longitudeSin + rotatedBLat * longitudeCos;

                bufferBuilder.addVertex(xCoord * 100 + vertexPosX, yCoord * 100 + rotatedALat, zCoord * 100 + vertexPosZ)
                        .setColor(r >> colorMultiplier, g << colorMultiplier, b >> colorMultiplier, currentAlpha)
                        .setUv(sprite.getU((corner & 2) >> 1), sprite.getV(((corner + 1) & 2) >> 1));
            }
        } else {
            // default
            for (int corner = 0; corner < 4; ++corner) {
                float offsetAlpha = 0.125f+alpha;
                bufferBuilder.addVertex(xCoord, yCoord, zCoord)
                        .setColor(r >> colorMultiplier, g << colorMultiplier, b >> colorMultiplier, seed)
                        .setUv(size/100.0f, connectedLines != 0 ? -offsetAlpha : offsetAlpha);
            }
        }
    }

    public Vector3f getRenderedPosition() {
        return new Vector3f(xCoord*100, yCoord*100, zCoord*100);
    }

    public int[] getColor() {
        return new int[]{r,g,b,(int) ((0.5f * alpha + 0.5f) * 255)};
    }

    public float[] getPosition() {
        return new float[]{xCoord, yCoord, zCoord};
    }

    public Vector3f getPositionAsVector3f() {
        return new Vector3f(xCoord, yCoord, zCoord);
    }

    public void connect() {
        connectedLines++;
    }

    public void disconnect() {
        if (connectedLines > 0) connectedLines -= 2;
    }

    public void clearAllConnections() {
        connectedLines = 0;
    }

    public float getAlpha() {
        return alpha;
    }

    public void select() {
        Constellation.deselect();
        OrbitingBody.deselect();
        if (selected != null) selected.isSelected = false;
        isSelected = true;
        selected = this;
        SpyglassAstronomyClient.spaceRenderingManager.scheduleStarsUpdate();
    }

    public static void deselect() {
        if (selected != null) {
            selected.isSelected = false;
            selected = null;
            SpyglassAstronomyClient.spaceRenderingManager.scheduleStarsUpdate();
        }
    }

    public boolean isUnnamed() {
        return name == null;
    }
}
