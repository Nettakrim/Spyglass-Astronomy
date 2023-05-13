package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AutoCommand {
    public static int generate(CommandContext<FabricClientCommandSource> context) {
        return 1;
    }

    public static int nameConstellations(CommandContext<FabricClientCommandSource> context) {
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            if (constellation.isUnnamed()) {
                constellation.name = generateConstellationName();
            }
        }
        return 1;
    }

    public static int nameStars(CommandContext<FabricClientCommandSource> context) {
        for (Star star : SpyglassAstronomyClient.stars) {
            if (star.isUnnamed()) {
                star.name = generateStarName();
            }
        }
        return 1;
    }

    public static int namePlanets(CommandContext<FabricClientCommandSource> context) {
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (orbitingBody.isUnnamed()) {
                orbitingBody.name = generatePlanetName();
            }
        }
        return 1;
    }

    private static String generateConstellationName() {
        return "";
    }

    private static String generateStarName() {
        return "";
    }

    private static String generatePlanetName() {
        return "";
    }
}
