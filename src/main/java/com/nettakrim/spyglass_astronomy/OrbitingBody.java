package com.nettakrim.spyglass_astronomy;

import java.util.ArrayList;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class OrbitingBody {
    private final Orbit orbit;
    private ArrayList<OrbitingBody> moons;

    public OrbitingBody(double period, double eccentricity, float rotation, float inclination) {
        this.orbit = new Orbit(period, eccentricity, rotation, inclination);
    }

    public void addMoon(OrbitingBody moon) {
        moons.add(moon);
    }

    public void setVertices(BufferBuilder bufferBuilder, Vec3f referencePosition, float t) {
        Vec3f position = orbit.getRotatedPositionAtGlobalTime(t);
        position.subtract(referencePosition);
        float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(position.getX(), position.getY(), position.getZ());
        position.scale(MathHelper.fastInverseSqrt(sqrDistance));

        float angle = 0;
        float size = 10;
        float xCoord = position.getX();
        float yCoord = position.getY();
        float zCoord = position.getZ();

        double polarAngle = Math.atan2(xCoord, zCoord);
        float longitudeSin = (float) Math.sin(polarAngle);
        float longitudeCos = (float) Math.cos(polarAngle);

        double proj = Math.atan2(Math.sqrt(xCoord  * xCoord  + zCoord * zCoord), yCoord);
        float latitudeSin = (float) Math.sin(proj);
        float latitudeCos = (float) Math.cos(proj);

        float angleSin = MathHelper.sin(angle);
        float angleCos = MathHelper.cos(angle);
        for (int corner = 0; corner < 4; ++corner) {
            float x = ((corner & 2) - 1) * size;
            float y = ((corner + 1 & 2) - 1) * size;
            float rotatedA = x * angleCos - y * angleSin;
            float rotatedB = y * angleCos + x * angleSin;
            float rotatedALat = rotatedA * latitudeSin;
            float rotatedBLat = -(rotatedA * latitudeCos);
            float vertexPosX = rotatedBLat * longitudeSin - rotatedB * longitudeCos;
            float vertexPosZ = rotatedB * longitudeSin + rotatedBLat * longitudeCos;
            bufferBuilder.vertex(xCoord*100 + vertexPosX, yCoord*100 + rotatedALat, zCoord*100 + vertexPosZ).color(255, 255, 255, 255).next();
        }
    }
}
