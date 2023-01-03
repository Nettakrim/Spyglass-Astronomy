package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

//https://github.com/ZtereoHYPE/nicer-skies/blob/main/src/main/java/codes/ztereohype/nicerskies/sky/star/Star.java

public class Star {
    public final int index;

    private final float xCoord;
    private final float yCoord;
    private final float zCoord;

    private final float longitudeSin;
    private final float longitudeCos;

    private final float latitudeSin;
    private final float latitudeCos;

    private final int r;
    private final int g;
    private final int b;
    private final float alpha;

    private float angle;
    private float size;

    private float rotationSpeed;
    private final float twinkleSpeed;
    private int currentAlpha;

    private int connectedStars = 0;

    public static Star selected;
    private boolean isSelected;

    public String name;

    public Star(int index, float posX, float posY, float posZ, float size, float rotationSpeed, int[] color, float alpha, float twinkleSpeed) {
        this.index = index;
 
        this.xCoord = posX;
        this.yCoord = posY;
        this.zCoord = posZ;
 
        this.r = color[0];
        this.g = color[1];
        this.b = color[2];
        this.alpha = alpha;

        double polarAngle = Math.atan2(posX, posZ);
        this.longitudeSin = (float) Math.sin(polarAngle);
        this.longitudeCos = (float) Math.cos(polarAngle);

        double proj = Math.atan2(Math.sqrt(posX * posX + posZ * posZ), posY);
        this.latitudeSin = (float) Math.sin(proj);
        this.latitudeCos = (float) Math.cos(proj);

        this.size = size;
        this.angle = rotationSpeed * MathHelper.PI;
        this.rotationSpeed = rotationSpeed * 0.005f;
        this.twinkleSpeed = twinkleSpeed;
    }

    public void update(int ticks) {
        angle += rotationSpeed;
        float twinkle = 1 - 2.5f * Math.max(MathHelper.sin(ticks*twinkleSpeed) - 0.75f,0);
        currentAlpha = (int) (getCurrentNonTwinkledAlpha() * twinkle * 255);
    }

    public float getCurrentNonTwinkledAlpha() {
        float heightScale = SpaceRenderingManager.getHeightScale();
        float brightness = heightScale*Math.max(alpha/2 + heightScale/2, 2*alpha-1) + (1-heightScale) * alpha * alpha * alpha;
        //float brightness = (((1-heightScale) * alpha * alpha * alpha + heightScale * heightScale));
        //brightness = MathHelper.clamp((brightness+alpha)/2, 3*alpha-2, brightness);
        if (connectedStars == 0) {
            return brightness;
        }
        return (brightness + (0.5f * alpha + 0.5f))/2;      
    }

    public void setVertices(BufferBuilder bufferBuilder) {
        float angleSin = MathHelper.sin(angle);
        float angleCos = MathHelper.cos(angle);
        int colorMult = isSelected ? 1 : 0;
        for (int corner = 0; corner < 4; ++corner) {
           float x = ((corner & 2) - 1) * size;
           float y = ((corner + 1 & 2) - 1) * size;
           float rotatedA = x * angleCos - y * angleSin;
           float rotatedB = y * angleCos + x * angleSin;
           float rotatedALat = rotatedA * latitudeSin;
           float rotatedBLat = -(rotatedA * latitudeCos);
           float vertexPosX = rotatedBLat * longitudeSin - rotatedB * longitudeCos;
           float vertexPosZ = rotatedB * longitudeSin + rotatedBLat * longitudeCos;
           bufferBuilder.vertex(xCoord*100 + vertexPosX, yCoord*100 + rotatedALat, zCoord*100 + vertexPosZ).color(r >> colorMult, g << colorMult, b >> colorMult, currentAlpha).next();
        }
    }

    public Vec3f getRenderedPosition() {
        return new Vec3f(xCoord*100, yCoord*100, zCoord*100);
    }

    public int[] getColor() {
        return new int[]{r,g,b,(int) ((0.5f * alpha + 0.5f) * 255)};
    }

    public float[] getPosition() {
        return new float[]{xCoord, yCoord, zCoord};
    }

    public Vec3f getPositionAsVec3f() {
        return new Vec3f(xCoord, yCoord, zCoord);
    }

    public void connect() {
        connectedStars++;
    }

    public void disconnect() {
        if (connectedStars > 0) connectedStars -= 2;
    }

    public void select() {
        Constellation.deselect();
        if (selected != null) selected.isSelected = false;
        isSelected = true;
        selected = this;
    }

    public static void deselect() {
        if (selected != null) selected.isSelected = false;
        selected = null;
    }
}
