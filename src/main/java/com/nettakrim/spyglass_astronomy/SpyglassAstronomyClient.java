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
    public static final String MODID = "spyglass_astronomy";
	public static final Logger LOGGER = LoggerFactory.getLogger("Spyglass Astronomy");

    public static boolean ready;

    private static final int starCount = 1024; //encoding will break at 4097, so stay at 4096 and below :)

    public static MinecraftClient client;
    public static ClientWorld world;

    public static ArrayList<Star> stars = new ArrayList<>();

    public static ArrayList<Constellation> constellations = new ArrayList<>();

    public static SpaceRenderingManager spaceRenderingManager;

    public static boolean isInEditMode;
    public static boolean isDrawingConstellation;
    private static StarLine drawingLine;
    public static Constellation drawingConstellation;
    public static Constellation activeConstellation;

    public static SpaceDataManager spaceDataManager;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
        client = MinecraftClient.getInstance();
        
	}

    public static void saveSpace() {
        if (spaceDataManager != null) {
            spaceDataManager.saveData();
        }
    }

    public static void loadSpace(ClientWorld clientWorld) {
        world = clientWorld;

        spaceDataManager = new SpaceDataManager(clientWorld);

        generateSpace();
    }

    public static void generateSpace() {
        Random random = Random.create(spaceDataManager.getStarSeed());

        int currentStars = 0;
        while (currentStars < starCount) {
            float posX = random.nextFloat() * 2.0f - 1.0f;
            float posY = random.nextFloat() * 2.0f - 1.0f;
            float posZ = random.nextFloat() * 2.0f - 1.0f;
            float galaxyBias = 0.75f;
            posX = (galaxyBias * posX * MathHelper.abs(posX))+((1-galaxyBias) * posX);

            //makes sure position is a uniform point in a sphere, then normalises position to the outside of a sphere
            float distance = posX * posX + posY * posY + posZ * posZ;
            if (!(distance < 1.0) || !(distance > 0.01)) continue;
            distance = MathHelper.fastInverseSqrt(distance);
            posX *= distance;
            posY *= distance;
            posZ *= distance;

            float sizeRaw = random.nextFloat();
            if (currentStars % 2 == 0) {
                float galaxyCloseness = (0.12f/(MathHelper.abs(posX)+0.1f))-0.2f;
                if (galaxyCloseness > 0) sizeRaw = (1-galaxyCloseness)*sizeRaw + galaxyCloseness*((sizeRaw*sizeRaw)/2);
            }
            float size = 0.15f + sizeRaw * 0.2f;
            float rotationSpeed = (random.nextFloat() * 2f)-1;

            float range = 0.8f;
            float offsetRange = 2*range-2;
            float gradientPos = random.nextFloat();
            float alphaRaw = random.nextFloat();
            float alpha = Math.max(MathHelper.sqrt(alphaRaw*sizeRaw),(2*sizeRaw-1.5f)/(alphaRaw+0.5f));
            alpha = (alpha + (alpha*alpha))/2;

            int[] color = new int[]{
                (int)(Math.min(offsetRange * gradientPos - range + 2f, 1f)*255),
                (int)(255 - random.nextFloat() * 20),
                (int)(Math.min(range - offsetRange * gradientPos, 1f)*255)
            };

            float twinkleSpeed = random.nextFloat()*0.025f+0.035f;

            stars.add(new Star(currentStars, posX, posY, posZ, size, rotationSpeed, color, alpha, twinkleSpeed));

            currentStars++;
        }

        ready = true;

        for (Constellation constellation : constellations) {
            constellation.initaliseStarLines();
        }

        spaceRenderingManager = new SpaceRenderingManager();
        spaceRenderingManager.UpdateSpace(0);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }

    public static void startUsingSpyglass() {
        isInEditMode = false;
    }

    public static void toggleEditMode() {
        isInEditMode = !isInEditMode;
    }

    public static void startDrawingConstellation() {
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());
        if (star == null) return;

        setActiveConstellation(star, true);

        drawingLine = new StarLine(star);
        drawingConstellation = new Constellation(drawingLine);

        drawingConstellation.isActive = true;
        isDrawingConstellation = true;

        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static float getSquaredDistance(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static void stopDrawingConstellation() {
        if (!isDrawingConstellation) return;
        isDrawingConstellation = false;

        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());

        if(star == null || !drawingLine.finishDrawing(star)) {
            return;
        }

        Constellation target = null;
        int end = constellations.size();
        for (int i = 0; i < end; i++) {
            Constellation constellation = constellations.get(i);
            if (constellation.lineIntersects(drawingLine)) {
                if (target != null) {
                    for (StarLine line : constellation.getLines()) {
                        target.addLine(line);
                    }
                    LOGGER.info("New StarLine merged two constellations");
                    constellations.remove(i);
                    break;
                }
                target = constellation;
            }
        }
        if (target != null) {
            Constellation potentialNew = target.addLineCanRemove(drawingLine);
            if (potentialNew != null) {
                LOGGER.info("New StarLine split constellation into two");
                constellations.add(potentialNew);
            }
        } else {
            constellations.add(drawingConstellation);
        }
        setActiveConstellation(star, false);

        spaceRenderingManager.scheduleConstellationsUpdate();
    }

    public static void updateDrawingConstellation() {
        if (!client.player.isUsingSpyglass()) {
            isDrawingConstellation = false;
            return;
        }
        Vec3f lookVector = getLookVector();
        rotateVectorToStarRotation(lookVector);
        Star star = getNearestStar(lookVector.getX(), lookVector.getY(), lookVector.getZ());
        if (star == null) {
            drawingLine.updateDrawing(new Vec3f(lookVector.getX() * 100, lookVector.getY() * 100, lookVector.getZ() * 100));
        } else {
            drawingLine.updateDrawing(star.getRenderedPosition());
        }
    }

    public static Vec3f getLookVector() {
        float pitch = client.player.getPitch() / 180 * MathHelper.PI;
        float yaw = client.player.getYaw() / 180 * MathHelper.PI;
        float x = -MathHelper.sin(yaw);
        float y = -MathHelper.sin(pitch);
        float z =  MathHelper.cos(yaw);
        float scale = MathHelper.cos(pitch);
        x *= scale;
        z *= scale;
        return new Vec3f(x, y, z);        
    }

    public static void rotateVectorToStarRotation(Vec3f vector) {
        vector.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        vector.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getPreciseMoonPhase()*-405f));
        vector.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45f));
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

        if (nearestDistance > 0.0005f) return null;

        if (nearestStar.getCurrentNonTwinkledAlpha() < 0.1f) return null;

        return nearestStar;
    }

    public static float getHeight() {
        if (client.player == null) return 128;
        return (float)client.player.getPos().y;
    }

    public static void setActiveConstellation(Star star, boolean clear) {
        boolean found = false;
        for (Constellation constellation : constellations) {
            constellation.isActive = false;
            if (constellation.hasStar(star)) {
                constellation.isActive = true;
                activeConstellation = constellation;
                found = true;
            }
        }
        if (!clear && !found && activeConstellation != null) {
            activeConstellation.isActive = true;
        }
    }  
}
