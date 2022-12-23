package com.nettakrim.spyglass_astronomy;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class SpyglassAstronomyClient implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("Spyglass Astronomy");

    private static final int starCount = 1024;

    public static MinecraftClient client;
    public static ClientWorld world;

    public static ArrayList<Star> stars = new ArrayList<>();

    public static ArrayList<Constellation> constellations = new ArrayList<>();

    public static SpaceRenderingManager spaceRenderingManager;

    public static boolean isUsingSpyglass;
    public static boolean isDrawingConstellation;
    private static StarLine drawingLine;
    public static Constellation drawingConstellation;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
        client = MinecraftClient.getInstance();
        
	}

    public static void generateStars() {
        world = client.world;

        Random random = Random.create(10L);

        int currentStars = 0;
        while (currentStars < starCount) {
            float posX = random.nextFloat() * 2.0f - 1.0f;
            float posY = random.nextFloat() * 2.0f - 1.0f;
            float posZ = random.nextFloat() * 2.0f - 1.0f;
            float galaxyBias = 0.5f;
            posX = (galaxyBias * posX * MathHelper.abs(posX))+((1-galaxyBias) * posX);

            //makes sure position is a uniform point in a sphere, then normalises position to the outside of a sphere
            float distance = posX * posX + posY * posY + posZ * posZ;
            if (!(distance < 1.0) || !(distance > 0.01)) continue;
            distance = MathHelper.fastInverseSqrt(distance);
            posX *= distance;
            posY *= distance;
            posZ *= distance;

            float sizeRaw = random.nextFloat();
            float size = 0.15f + sizeRaw * 0.2f;
            float rotationSpeed = (random.nextFloat() * 2f)-1;

            float range = 0.8f;
            float offsetRange = 2*range-2;
            float gradientPos = random.nextFloat();
            float alphaRaw = random.nextFloat();
            int[] color = new int[]{
                (int)(Math.min(offsetRange * gradientPos - range + 2f, 1f)*255),
                (int)(255 - random.nextFloat() * 20),
                (int)(Math.min(range - offsetRange * gradientPos, 1f)*255),
                (int)(Math.max(MathHelper.sqrt(alphaRaw*sizeRaw),(2*sizeRaw-1.5f)/(alphaRaw+0.5f))*255f)
            };

            float twinkleSpeed = random.nextFloat()*0.025f+0.035f;

            stars.add(new Star(currentStars, posX, posY, posZ, size, rotationSpeed, color, twinkleSpeed));

            currentStars++;
        }

        spaceRenderingManager = new SpaceRenderingManager();
        spaceRenderingManager.UpdateSpace(0);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }

    public static void startUsingSpyglass() {
        isUsingSpyglass = true;
    }

    public static void stopUsingSpyglass() {
        isUsingSpyglass = false;
    }

    public static void startDrawingConstellation() {
        float[] lookVector = getLookVector();
        Star star = getNearestStar(lookVector[0], lookVector[1], lookVector[2]);
        if (star == null) return;

        drawingLine = new StarLine(star);
        drawingConstellation = new Constellation(drawingLine);

        isDrawingConstellation = true;
    }

    public static float getSquaredDistance(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static void stopDrawingConstellation() {
        isDrawingConstellation = false;

        float[] lookVector = getLookVector();
        Star star = getNearestStar(lookVector[0], lookVector[1], lookVector[2]);

        if (star == null) {
            return;
        }

        if(!drawingLine.finishDrawing(star)) {
            return;
        }

        Constellation target = null;
        int end = constellations.size();
        for (int i = 0; i < end; i++) {
            Constellation constellation = constellations.get(i);
            if (constellation.lineIntersects(drawingLine)) {
                if (target != null) {
                    LOGGER.info("New StarLine would merge two constellations");
                    return;
                }
                target = constellation;
            }
        }
        if (target != null) {
            target.addLine(drawingLine, true);
        } else {
            constellations.add(drawingConstellation);
        }
        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static void updateDrawingConstellation() {
        if (!isUsingSpyglass) {
            isDrawingConstellation = false;
            return;
        }
        float[] lookVector = getLookVector();
        Star star = getNearestStar(lookVector[0], lookVector[1], lookVector[2]);
        if (star == null) {
            drawingLine.updateDrawing(new Vec3f(lookVector[0] * 100, lookVector[1] * 100, lookVector[2] * 100));
        } else {
            drawingLine.updateDrawing(star.getRenderedPosition());
        }
    }

    public static float[] getLookVector() {
        float pitch = client.player.getPitch() / 180 * MathHelper.PI;
        float yaw = client.player.getYaw() / 180 * MathHelper.PI;
        float x = -MathHelper.sin(yaw);
        float y = -MathHelper.sin(pitch);
        float z =  MathHelper.cos(yaw);
        float scale = MathHelper.cos(pitch);
        x *= scale;
        z *= scale;
        return new float[]{x, y, z};        
    }

    public static Star getNearestStar(float x, float y, float z) {
        float nearestDistance = 5; //at most the nearest star can only be 2 units away, or 4 when squared
        Star nearestStar = null;

        for (Star star : stars) {
            float[] pos = star.getPosition();
            float currentDistance = getSquaredDistance(x - pos[0], y - pos[1], z - pos[2]);
            if (currentDistance < nearestDistance) {
                nearestDistance = currentDistance;
                nearestStar = star;
            }
        }

        if (nearestDistance > 1) return null;

        return nearestStar;
    }

    public static float getHeightScale() {
        if (client.player == null) return 1;
        return MathHelper.clamp((((float)client.player.getPos().y)-64f)/192f, 0f, 1f);
    }
}
