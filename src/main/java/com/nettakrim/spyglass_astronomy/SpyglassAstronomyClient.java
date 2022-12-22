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

    private static final int starCount = 1500;

    public static MinecraftClient client;
    public static ClientWorld world;

    public static ArrayList<Star> stars = new ArrayList<>();

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
        for (int i = 0; i < starCount; ++i) {
            float posX = random.nextFloat() * 2.0f - 1.0f;
            float posY = random.nextFloat() * 2.0f - 1.0f;
            float posZ = random.nextFloat() * 2.0f - 1.0f;

            float h = posX * posX + posY * posY + posZ * posZ;
            if (!(h < 1.0) || !(h > 0.01)) continue;
            h = MathHelper.fastInverseSqrt(h);
            posX *= h;
            posY *= h;
            posZ *= h;

            float sizeRaw = random.nextFloat();
            float size = 0.15f + sizeRaw * 0.1f;
            float rotationSpeed = (random.nextFloat() * 2f)-1;

            float range = 0.8f;
            float offsetRange = 2*range-2;
            float gradientPos = random.nextFloat();
            int[] color = new int[]{
                (int)(Math.min(offsetRange * gradientPos - range + 2f, 1f)*255),
                255,
                (int)(Math.min(range - offsetRange * gradientPos, 1f)*255),
                //(int)(((random.nextFloat()+sizeRaw)/2)*255f)
                (int)(MathHelper.sqrt(random.nextFloat()*sizeRaw)*255f)
            };

            float twinkleSpeed = random.nextFloat()*0.01f+0.025f;

            stars.add(new Star(posX, posY, posZ, size, rotationSpeed, color, twinkleSpeed));
        }
        starRenderingManager = new StarRenderingManager();
        starRenderingManager.UpdateStars(0);
    }

    public static float getPreciseMoonPhase() {
        return (world.getLunarTime()%24000/24000.0f)+(world.getMoonPhase());
    }
}
