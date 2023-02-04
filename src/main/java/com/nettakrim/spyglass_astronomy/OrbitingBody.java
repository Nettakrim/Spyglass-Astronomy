package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

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

    private Vector3f axis1;
    private Vector3f axis2;
    private Vector3f quad1vertex1;
    private Vector3f quad1vertex2;
    private Vector3f quad1vertex3;
    private Vector3f quad1vertex4;
    private Vector3f quad2vertex1;
    private Vector3f quad2vertex2;
    private Vector3f quad2vertex3;
    private Vector3f quad2vertex4;    

    private Vector3f position;

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

    public void update(int ticks, Vector3f referencePosition, Vector3f normalisedReferencePosition, Long day, float dayFraction) {
        angle = (angle+rotationSpeed)%360;

        position = orbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
        
        Vector3f similarityVector = new Vector3f(position);
        similarityVector.normalize();
        float similarity = similarityVector.dot(normalisedReferencePosition);
    
        position.sub(referencePosition);
        float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(position.x, position.y, position.z);
        float inverseSqrt = MathHelper.fastInverseSqrt(sqrDistance);
        position.mul(inverseSqrt);

        float distance = (1/inverseSqrt)/SpyglassAstronomyClient.earthOrbit.semiMajorAxis;

        float visibilityScale = Math.min(MathHelper.sqrt(distance),8);

        //this isnt needed to run every frame
        {
            //it may seem a bit weird allowing dayFraction to be outside of 0-1, but it doesnt matter
            axis1 = orbit.getRotatedPositionAtGlobalTime(day, dayFraction-(orbit.period/32), false);
            axis1.sub(referencePosition);
            axis1.normalize();
            axis1.sub(position);
            axis1.normalize();

            axis2 = new Vector3f(axis1);
            axis2.rotate(RotationAxis.of(position).rotationDegrees(90));
        
            float sizeScale = MathHelper.clamp(
                (size/visibilityScale)*3,
            0.25f,1.5f);

            axis1.mul(sizeScale);
            axis2.mul(sizeScale);
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

        Quaternionf rotation = RotationAxis.of(position).rotationDegrees(angle);
        Vector3f rotatedAxis1 = new Vector3f(axis1);
        Vector3f rotatedAxis2 = new Vector3f(axis2);
        rotatedAxis1.rotate(rotation);
        rotatedAxis2.rotate(rotation);

        float x = position.x*100;
        float y = position.y*100;
        float z = position.z*100;
        quad1vertex1 = new Vector3f(x, y, z);
        quad1vertex2 = new Vector3f(x, y, z);
        quad1vertex3 = new Vector3f(x, y, z);
        quad1vertex4 = new Vector3f(x, y, z);
        quad1vertex1.sub(rotatedAxis2);
        quad1vertex2.sub(rotatedAxis1);
        quad1vertex3.add(rotatedAxis2);
        quad1vertex4.add(rotatedAxis1);        

        if (isPlanet) {
            switch (decoration) {
                case 0:
                    //triangle half
                    quad2vertex1 = new Vector3f(x, y, z);
                    quad2vertex2 = new Vector3f(quad1vertex2);
                    quad2vertex3 = new Vector3f(quad1vertex3);
                    quad2vertex4 = new Vector3f(quad1vertex4);
                    break;
                case 1:
                    //ring
                    float ringOut = 1.3f;
                    float ringIn = 0.9f;
                    Quaternionf slowOppositeRotation = RotationAxis.of(position).rotationDegrees(-angle/2);
                    Vector3f in1 = new Vector3f(axis1);
                    in1.rotate(slowOppositeRotation);
                    Vector3f out1 = new Vector3f(in1);
                    Vector3f in2 = new Vector3f(axis2);
                    in2.rotate(slowOppositeRotation);
                    Vector3f out2 = new Vector3f(in2);
                    in1.mul(ringIn);
                    out1.mul(ringOut);
                    in2.mul(ringIn);
                    out2.mul(ringOut);
                    quad2vertex1 = new Vector3f(x, y, z);
                    quad2vertex2 = new Vector3f(x, y, z);
                    quad2vertex3 = new Vector3f(x, y, z);
                    quad2vertex4 = new Vector3f(x, y, z);
                    quad2vertex1.sub(out2);
                    quad2vertex1.sub(in1);
                    quad2vertex2.sub(out1);
                    quad2vertex2.sub(in2);  
                    quad2vertex3.add(out2);
                    quad2vertex3.add(in1);
                    quad2vertex4.add(out1);
                    quad2vertex4.add(in2);
                    break;
                case 2,3:
                    //quater, quater with point
                    if (decoration == 2) quad2vertex1 = new Vector3f(x, y, z);
                    else quad2vertex1 = new Vector3f(quad1vertex1);
                    quad2vertex2 = new Vector3f(x, y, z);
                    quad2vertex3 = new Vector3f(quad1vertex3);
                    quad2vertex4 = new Vector3f(x, y, z);
                    Vector3f offset1 = new Vector3f(rotatedAxis2);
                    Vector3f offset2 = new Vector3f(rotatedAxis2);
                    offset1.add(rotatedAxis1);
                    offset2.sub(rotatedAxis1);
                    offset1.mul(0.5f);
                    offset2.mul(0.5f);
                    quad2vertex2.add(offset2);
                    quad2vertex4.add(offset1);
                    break;
            }
        } else {
            quad2vertex1 = new Vector3f(x, y, z);
            quad2vertex2 = new Vector3f(x, y, z);
            quad2vertex3 = new Vector3f(x, y, z);
            quad2vertex4 = new Vector3f(x, y, z);
            Vector3f trailEnd = new Vector3f(axis1);
            trailEnd.mul(6);
            quad2vertex1.add(trailEnd);
            quad2vertex2.add(trailEnd);
            quad2vertex3.add(axis1);
            quad2vertex4.add(axis1);

            Vector3f trailWidth = new Vector3f(axis2);
            trailWidth.mul(0.75f);

            if (decoration != 0) {
                quad2vertex1.add(trailWidth);
                quad2vertex2.sub(trailWidth);                
            }
            if (decoration != 2) {
                quad2vertex3.sub(trailWidth);
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
            quad1vertex1.x,
            quad1vertex1.y,
            quad1vertex1.z
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex2.x,
            quad1vertex2.y,
            quad1vertex2.z
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex3.x,
            quad1vertex3.y,
            quad1vertex3.z
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad1vertex4.x,
            quad1vertex4.y,
            quad1vertex4.z
        ).color(r1, g1, b1, currentAlpha).next();

        bufferBuilder.vertex(
            quad2vertex1.x,
            quad2vertex1.y,
            quad2vertex1.z
        ).color(r2, g2, b2, isPlanet ? decorationAlpha : 0).next();

        bufferBuilder.vertex(
            quad2vertex2.x,
            quad2vertex2.y,
            quad2vertex2.z
        ).color(r2, g2, b2, isPlanet ? decorationAlpha : 0).next();

        bufferBuilder.vertex(
            quad2vertex3.x,
            quad2vertex3.y,
            quad2vertex3.z
        ).color(r2, g2, b2, decorationAlpha).next();

        bufferBuilder.vertex(
            quad2vertex4.x,
            quad2vertex4.y,
            quad2vertex4.z
        ).color(r2, g2, b2, decorationAlpha).next();        
    }

    public Vector3f getPosition() {
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
