package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class OrbitingBody {
    public final Orbit orbit;
    private ArrayList<OrbitingBody> moons;

    private final float size;
    private final float albedo;
    private final float rotationSpeed;
    public final boolean isPlanet;
    private final int decoration;
    private final int[] mainColor;
    private final int[] secondaryColor;
    public final OrbitingBodyType type;

    private float angle;
    private int currentAlpha;

    private Vec3f axis1;
    private Vec3f axis2;
    private Vec3f quad1vertex1;
    private Vec3f quad1vertex2;
    private Vec3f quad1vertex3;
    private Vec3f quad1vertex4;
    private Vec3f quad2vertex1;
    private Vec3f quad2vertex2;
    private Vec3f quad2vertex3;
    private Vec3f quad2vertex4;    

    private Vec3f position;

    public String name;

    public static OrbitingBody selected;
    private boolean isSelected;

    public OrbitingBody(Orbit orbit, float size, float albedo, float rotationSpeed, boolean isPlanet, int decoration, int[] mainColor, int[] secondaryColor, OrbitingBodyType type) {
        this.orbit = orbit;
        this.size = size;
        this.albedo = albedo;
        this.rotationSpeed = rotationSpeed * 0.1f;
        this.isPlanet = isPlanet;
        this.decoration = decoration;
        this.mainColor = mainColor;
        this.secondaryColor = secondaryColor;
        this.type = type;
    }

    public void addMoon(OrbitingBody moon) {
        moons.add(moon);
    }

    public void update(int ticks, Vec3f referencePosition, Vec3f normalisedReferencePosition, Long day, float dayFraction) {
        angle = (angle+rotationSpeed)%360;

        position = orbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
        
        Vec3f similarityVector = position.copy();
        similarityVector.normalize();
        float similarity = similarityVector.dot(normalisedReferencePosition);
    
        position.subtract(referencePosition);
        float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(position.getX(), position.getY(), position.getZ());
        float inverseSqrt = MathHelper.fastInverseSqrt(sqrDistance);
        position.scale(inverseSqrt);

        float distance = (1/inverseSqrt)/SpyglassAstronomyClient.earthOrbit.semiMajorAxis;

        float visibilityScale = Math.min(MathHelper.sqrt(distance),8);

        //this isnt needed to run every frame
        {
            //it may seem a bit weird allowing dayFraction to be outside of 0-1, but it doesnt matter
            axis1 = orbit.getRotatedPositionAtGlobalTime(day, dayFraction-(orbit.period/32), false);
            axis1.subtract(referencePosition);
            axis1.normalize();
            axis1.subtract(position);
            axis1.normalize();

            axis2 = axis1.copy();
            axis2.rotate(position.getDegreesQuaternion(90));
        
            float sizeScale = MathHelper.clamp(
                (size/visibilityScale)*3,
            0.25f,1.5f);

            axis1.scale(sizeScale);
            axis2.scale(sizeScale);
        }
        
        float heightScale = SpaceRenderingManager.getHeightScale();
        float heightFactor = 1;

        float alphaRaw = Math.max(((
                    Math.min(
                        albedo*10*heightScale-visibilityScale+heightFactor,
                        1
                    )+heightScale
                )/2
            ),
            0
        );
        if (similarity < -0.5) {
            float offsetSimilarity = 2*(similarity+0.5f);
            alphaRaw *= 1-offsetSimilarity*offsetSimilarity;
        }
        currentAlpha = (int)(alphaRaw*255);

        Quaternion rotation = position.getDegreesQuaternion(angle);
        Vec3f rotatedAxis1 = axis1.copy();
        Vec3f rotatedAxis2 = axis2.copy();
        rotatedAxis1.rotate(rotation);
        rotatedAxis2.rotate(rotation);

        float x = position.getX()*100;
        float y = position.getY()*100;
        float z = position.getZ()*100;
        quad1vertex1 = new Vec3f(x, y, z);
        quad1vertex2 = new Vec3f(x, y, z);
        quad1vertex3 = new Vec3f(x, y, z);
        quad1vertex4 = new Vec3f(x, y, z);
        quad1vertex1.subtract(rotatedAxis2);
        quad1vertex2.subtract(rotatedAxis1);
        quad1vertex3.add(rotatedAxis2);
        quad1vertex4.add(rotatedAxis1);        

        if (isPlanet) {
            switch (decoration) {
                case 0:
                    //triangle half
                    quad2vertex1 = new Vec3f(x, y, z);
                    quad2vertex2 = quad1vertex2.copy();
                    quad2vertex3 = quad1vertex3.copy();
                    quad2vertex4 = quad1vertex4.copy();
                    break;
                case 1:
                    //ring
                    float ringOut = 1.3f;
                    float ringIn = 0.9f;
                    Quaternion slowOppositeRotation = position.getDegreesQuaternion(-angle/2);
                    Vec3f in1 = axis1.copy();
                    in1.rotate(slowOppositeRotation);
                    Vec3f out1 = in1.copy();
                    Vec3f in2 = axis2.copy();
                    in2.rotate(slowOppositeRotation);
                    Vec3f out2 = in2.copy();
                    in1.scale(ringIn);
                    out1.scale(ringOut);
                    in2.scale(ringIn);
                    out2.scale(ringOut);
                    quad2vertex1 = new Vec3f(x, y, z);
                    quad2vertex2 = new Vec3f(x, y, z);
                    quad2vertex3 = new Vec3f(x, y, z);
                    quad2vertex4 = new Vec3f(x, y, z);
                    quad2vertex1.subtract(out2);
                    quad2vertex1.subtract(in1);
                    quad2vertex2.subtract(out1);
                    quad2vertex2.subtract(in2);  
                    quad2vertex3.add(out2);
                    quad2vertex3.add(in1);
                    quad2vertex4.add(out1);
                    quad2vertex4.add(in2);
                    break;
                case 2,3:
                    //quater, quater with point
                    if (decoration == 2) quad2vertex1 = new Vec3f(x, y, z);
                    else quad2vertex1 = quad1vertex1.copy();
                    quad2vertex2 = new Vec3f(x, y, z);
                    quad2vertex3 = quad1vertex3.copy();
                    quad2vertex4 = new Vec3f(x, y, z);
                    Vec3f offset1 = rotatedAxis2.copy();
                    Vec3f offset2 = rotatedAxis2.copy();
                    offset1.add(rotatedAxis1);
                    offset2.subtract(rotatedAxis1);
                    offset1.scale(0.5f);
                    offset2.scale(0.5f);
                    quad2vertex2.add(offset2);
                    quad2vertex4.add(offset1);
                    break;
            }
        } else {
            quad2vertex1 = new Vec3f(x, y, z);
            quad2vertex2 = new Vec3f(x, y, z);
            quad2vertex3 = new Vec3f(x, y, z);
            quad2vertex4 = new Vec3f(x, y, z);
            Vec3f trailEnd = axis1.copy();
            trailEnd.scale(6);
            quad2vertex1.add(trailEnd);
            quad2vertex2.add(trailEnd);
            quad2vertex3.add(axis1);
            quad2vertex4.add(axis1);

            Vec3f trailWidth = axis2.copy();
            trailWidth.scale(0.75f);

            if (decoration != 0) {
                quad2vertex1.add(trailWidth);
                quad2vertex2.subtract(trailWidth);                
            }
            if (decoration != 2) {
                quad2vertex3.subtract(trailWidth);
                quad2vertex4.add(trailWidth);
            }
        }
    }

    public void setVertices(BufferBuilder bufferBuilder) {
        int colorMult = isSelected ? 1 : 0;
        int r1 = mainColor[0] >> colorMult;
        int g1 = mainColor[1];
        int b1 = mainColor[2] >> colorMult;
        int r2 = secondaryColor[0] >> colorMult;
        int g2 = secondaryColor[1];
        int b2 = secondaryColor[2] >> colorMult;        
        int decorationAlpha = currentAlpha/3;

        bufferBuilder.vertex(
            quad1vertex1.getX(),
            quad1vertex1.getY(),
            quad1vertex1.getZ()
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex2.getX(),
            quad1vertex2.getY(),
            quad1vertex2.getZ()
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex3.getX(),
            quad1vertex3.getY(),
            quad1vertex3.getZ()
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex4.getX(),
            quad1vertex4.getY(),
            quad1vertex4.getZ()
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad2vertex1.getX(),
            quad2vertex1.getY(),
            quad2vertex1.getZ()
        ).color(r2, g2, b2, isPlanet ? decorationAlpha : 0).next();

        bufferBuilder.vertex(
            quad2vertex2.getX(),
            quad2vertex2.getY(),
            quad2vertex2.getZ()
        ).color(r2, g2, b2, isPlanet ? decorationAlpha : 0).next();

        bufferBuilder.vertex(
            quad2vertex3.getX(),
            quad2vertex3.getY(),
            quad2vertex3.getZ()
        ).color(r2, g2, b2, decorationAlpha).next();

        bufferBuilder.vertex(
            quad2vertex4.getX(),
            quad2vertex4.getY(),
            quad2vertex4.getZ()
        ).color(r2, g2, b2, decorationAlpha).next();        
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

    public enum OrbitingBodyType {
        //ordered roughly by rquired closeness to sun
        TERRESTIAL,
        HABITABLE,
        OCEANPLANET,
        ICEPLANET,
        GASGIANT,
        ICEGIANT,
        COMET
    }
}
