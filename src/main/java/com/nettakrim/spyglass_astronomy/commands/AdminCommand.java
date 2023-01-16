package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AdminCommand {
    public static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.setStarCount(IntegerArgumentType.getInteger(context, "amount"));
        SpyglassAstronomyClient.generateStars(null);
        return 1;
    }

    public static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(LongArgumentType.getLong(context, "seed"));
        SpyglassAstronomyClient.generateStars(null);
        return 1;
    }

    public static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(LongArgumentType.getLong(context, "seed"));
        SpyglassAstronomyClient.generatePlanets(null);
        return 1;
    }    

    public static int removeConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        SpyglassAstronomyClient.constellations.remove(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        return 1;
    }

    public static int discardUnsavedChanges(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.spaceDataManager.loadData();
        SpyglassAstronomyClient.generateSpace();
        return 1;
    }
}
