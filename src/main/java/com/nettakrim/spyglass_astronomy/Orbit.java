package com.nettakrim.spyglass_astronomy;

import org.joml.Vector3f;

import net.minecraft.util.Mth;
import com.mojang.math.Axis;

public class Orbit {
    public final float period;
    public final float eccentricity;
    public final float semiMajorAxis;
    public final float distance;
    public final float rotation;
    public final float ascension;
    public final float inclination;
    public final float timeOffset;

    public float lastLocalTime;

    //orbit speed scale, k = 1 means that a period of 1 lasts 1 minecraft day
    private final static double k = 1;

    public Orbit(double period, double eccentricity, float rotation, float ascension, float inclination, float timeOffset) {
        double semiMajorAxis = Math.cbrt((period*period)/k);
        double distance = semiMajorAxis*(1-eccentricity*eccentricity);
        
        this.period = (float) period;
        this.eccentricity = (float) eccentricity;
        this.semiMajorAxis = (float) semiMajorAxis;
        this.distance = (float) distance;

        this.rotation = rotation;
        this.ascension = ascension;
        this.inclination = inclination;
        this.timeOffset = timeOffset;
    }

    public float getLocalAngleAtLocalTime(float t) {
        return KeplerLookup.getAt(eccentricity, (t%1)*2);
    }

    public Vector3f getLocalPositionAtLocalTime(float t, boolean updateLastPos) {
        if (t < 0) t++;
        if (updateLastPos) this.lastLocalTime = t;
        float f = getLocalAngleAtLocalTime(t);
        float cosAngle = Mth.cos(f);
        float scale = distance/(1+eccentricity*cosAngle);
        return new Vector3f(cosAngle * scale, Mth.sin(f) * scale, 0);
    }

    public Vector3f getRotatedPositionAtGlobalTime(Long day, float dayFraction, boolean updateLastPos) {
        Vector3f pos = getLocalPositionAtLocalTime((((day%period)/period)+(dayFraction/period)+timeOffset)%1, updateLastPos);
        rotateLocalPosition(pos);
        return pos;
    }

    public void rotateLocalPosition(Vector3f vector) {
        vector.rotate(Axis.XP.rotationDegrees(inclination));
        vector.rotate(Axis.YP.rotationDegrees(ascension));
        vector.rotate(Axis.ZP.rotationDegrees(rotation));
    }

    public Vector3f getLastRotatedPosition() {
        Vector3f pos = getLocalPositionAtLocalTime(lastLocalTime, false);
        rotateLocalPosition(pos);
        return pos;
    }
}
