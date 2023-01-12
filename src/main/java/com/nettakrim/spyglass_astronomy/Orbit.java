package com.nettakrim.spyglass_astronomy;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class Orbit {
    public final float period;
    public final float eccentricity;
    public final float semiMajorAxis;
    public final float distance;
    public final float rotation;
    public final float inclination;

    //orbit speed scale, k = 1 means that a period of 1 lasts 1 minecraft day
    private final static double k = 1;

    public Orbit(double period, double eccentricity, float rotation, float inclination) {
        double semiMajorAxis = Math.cbrt((period*period)/k);
        double distance = semiMajorAxis*(1-eccentricity*eccentricity);
        
        this.period = (float) period;
        this.eccentricity = (float) eccentricity;
        this.semiMajorAxis = (float) semiMajorAxis;
        this.distance = (float) distance;

        this.rotation = rotation;
        this.inclination = inclination;
    }

    public float getLocalAngleAtLocalTime(float t) {
        return KeplerLookup.getAt(eccentricity, (t%1)*2);
    }

    public Vec3f getLocalPositionAtLocalTime(float t) {
        float f = getLocalAngleAtLocalTime(t);
        float cosAngle = MathHelper.cos(f);
        float scale = distance/(1+eccentricity*cosAngle);
        return new Vec3f(cosAngle * scale, MathHelper.sin(f) * scale, 0);
    }

    public Vec3f getRotatedPositionAtGlobalTime(float t) {
        Vec3f pos = getLocalPositionAtLocalTime(t/period);
        rotateLocalPosition(pos);
        return pos;
    }

    public void rotateLocalPosition(Vec3f vector) {
        vector.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(inclination));
        vector.rotate(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation));
    }
}
