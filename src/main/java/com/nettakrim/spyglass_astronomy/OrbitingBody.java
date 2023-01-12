package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class OrbitingBody {
    public final Orbit orbit;
    private ArrayList<OrbitingBody> moons;

    private Vec3f lastPosition = new Vec3f();

    private final float size;
    private final float albedo;
    private final float rotationSpeed;

    private float angle;
    private int currentAlpha;

    private Vec3f axis1;
    private Vec3f axis2;
    private Vec3f vertex1;
    private Vec3f vertex2;
    private Vec3f vertex3;
    private Vec3f vertex4;

    private Vec3f position;

    public String name;

    public static OrbitingBody selected;
    private boolean isSelected;

    public OrbitingBody(Orbit orbit, float size, float albedo, float rotationSpeed) {
        this.orbit = orbit;
        this.size = size;
        this.albedo = albedo;
        this.rotationSpeed = rotationSpeed * 0.01f;
    }

    public void addMoon(OrbitingBody moon) {
        moons.add(moon);
    }

    public void update(int ticks, Vec3f referencePosition, Vec3f normalisedReferencePosition, float t) {
        angle = (angle+rotationSpeed)%90;

        position = orbit.getRotatedPositionAtGlobalTime(t);
        
        Vec3f similarityVector = position.copy();
        similarityVector.normalize();
        float similarity = similarityVector.dot(normalisedReferencePosition);
    
        position.subtract(referencePosition);
        float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(position.getX(), position.getY(), position.getZ());
        float inverseSqrt = MathHelper.fastInverseSqrt(sqrDistance);
        position.scale(inverseSqrt);

        float distanceSinceLastCalculation = SpyglassAstronomyClient.getSquaredDistance(position.getX()-lastPosition.getX(), position.getY()-lastPosition.getY(), position.getZ()-lastPosition.getZ());
        float distanceScale = MathHelper.clamp(MathHelper.sqrt(100*inverseSqrt), 0.5f, 10f);
        if (distanceSinceLastCalculation > 0.001f) {
            axis1 = lastPosition.copy();
            axis1.subtract(position);
            axis1.normalize();
            Quaternion rotation = position.getDegreesQuaternion(90);
            axis2 = axis1.copy();
            axis2.rotate(rotation);
        
            float distanceSizeScale = size * Math.min(distanceScale,2f) * Math.min((similarity+1f),0.5f);
            axis1.scale(distanceSizeScale);
            axis2.scale(distanceSizeScale);

            lastPosition = position.copy();
        }

        float heightScale = SpaceRenderingManager.getHeightScale() + 0.5f;
        float alphaRaw = (albedo * (12*distanceScale*heightScale + 135*heightScale)) * Math.min((similarity+1),1f);
        float distanceHeightProportion = Math.min(distanceScale-0.5f,3f)/3f;
        alphaRaw = (1-distanceHeightProportion)*Math.max(2*alphaRaw-1,0) + distanceHeightProportion*alphaRaw;
        currentAlpha = Math.min((int)alphaRaw,255);

        Quaternion rotation = position.getDegreesQuaternion(angle);
        Vec3f rotatedAxis1 = axis1.copy();
        Vec3f rotatedAxis2 = axis2.copy();
        rotatedAxis1.rotate(rotation);
        rotatedAxis2.rotate(rotation);

        float x = position.getX()*100;
        float y = position.getY()*100;
        float z = position.getZ()*100;
        vertex1 = new Vec3f(x-rotatedAxis2.getX(), y-rotatedAxis2.getY(), z-rotatedAxis2.getZ());
        vertex2 = new Vec3f(x-rotatedAxis1.getX(), y-rotatedAxis1.getY(), z-rotatedAxis1.getZ());
        vertex3 = new Vec3f(x+rotatedAxis2.getX(), y+rotatedAxis2.getY(), z+rotatedAxis2.getZ());
        vertex4 = new Vec3f(x+rotatedAxis1.getX(), y+rotatedAxis1.getY(), z+rotatedAxis1.getZ());
    }

    public void setVertices(BufferBuilder bufferBuilder) {
        int colorMult = isSelected ? 1 : 0;
        int r = 255 >> colorMult;
        int g = 255;
        int b = 255 >> colorMult;

        bufferBuilder.vertex(
            vertex1.getX(),
            vertex1.getY(),
            vertex1.getZ()
        ).color(r, g, b, currentAlpha).next();

        bufferBuilder.vertex(
            vertex2.getX(),
            vertex2.getY(),
            vertex2.getZ()
        ).color(r, g, b, currentAlpha).next();

        bufferBuilder.vertex(
            vertex3.getX(),
            vertex3.getY(),
            vertex3.getZ()
        ).color(r, g, b, currentAlpha).next();

        bufferBuilder.vertex(
            vertex4.getX(),
            vertex4.getY(),
            vertex4.getZ()
        ).color(r, g, b, currentAlpha).next();
    }

    public Vec3f getPosition() {
        return position;
    }

    public float getCurrentNonTwinkledAlpha() {
        return ((float)currentAlpha)/255;
    }

    public void select() {
        Constellation.deselect();
        Star.deselect();
        if (selected != null) selected.isSelected = false;
        isSelected = true;
        selected = this;
    }

    public static void deselect() {
        if (selected != null) selected.isSelected = false;
        selected = null;
    }
}
