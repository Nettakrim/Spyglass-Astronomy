package com.nettakrim.spyglass_astronomy;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import net.minecraft.util.math.MathHelper;

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

    public static StarRenderingManager starRenderingManager;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
        client = MinecraftClient.getInstance();
        
	}

    public static void GenerateStars() {
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
            int[] color = new int[]{
                (int)(Math.min(offsetRange * gradientPos - range + 2f, 1f)*255),
                (int)(255 - random.nextFloat() * 20),
                (int)(Math.min(range - offsetRange * gradientPos, 1f)*255),
                //(int)(((random.nextFloat()+sizeRaw)/2)*255f)
                (int)(MathHelper.sqrt(random.nextFloat()*sizeRaw)*255f)
            };

            float twinkleSpeed = random.nextFloat()*0.025f+0.035f;

            stars.add(new Star(posX, posY, posZ, size, rotationSpeed, color, twinkleSpeed));

            currentStars++;
        }

        starRenderingManager = new StarRenderingManager();
        starRenderingManager.UpdateStars(0);

        Constellation constellation = new Constellation();
        for (int i = 0; i < starCount; i++) {
            Star star = stars.get(i);
            float[] position = star.getPosition();
            for (int i2 = 0; i2 < starCount-i; i2++) {
                if (random.nextFloat() < 0.7f) continue;
                Star star2 = stars.get(i2);
                float[] position2 = star2.getPosition();
                float distance = MathHelper.abs((position[0]-position2[0]))+MathHelper.abs((position[1]-position2[1]))+MathHelper.abs((position[2]-position2[2]));
                if (distance != 0 && distance < random.nextFloat()/4) {
                    constellation.AddLine(i, i2);
                }
            }    
        }
        constellations.add(constellation);

        starRenderingManager.UpdateConstellations(0);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }
}
