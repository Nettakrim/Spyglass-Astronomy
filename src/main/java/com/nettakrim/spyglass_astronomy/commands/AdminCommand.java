package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.StarLine;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AdminCommand {
    public static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.setStarCount(IntegerArgumentType.getInteger(context, "amount"));
        SpyglassAstronomyClient.generateStars(null, true);
        return 1;
    }

    public static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(LongArgumentType.getLong(context, "seed"));
        SpyglassAstronomyClient.generateStars(null, true);
        return 1;
    }

    public static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(LongArgumentType.getLong(context, "seed"));
        SpyglassAstronomyClient.generatePlanets(null, true);
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
        SpyglassAstronomyClient.generateSpace(true);
        return 1;
    }

    public static int addConstellation(CommandContext<FabricClientCommandSource> context) {
        String dataRaw = SpyglassAstronomyCommands.getMessageText(context,"data");
        int index = dataRaw.indexOf(' ');
        Constellation constellation = SpaceDataManager.decodeConstellation(null, dataRaw.substring(index+1), dataRaw.substring(0, index));
        constellation.initaliseStarLines();
        for (Constellation targetConstellation : SpyglassAstronomyClient.constellations) {
            for (StarLine line : constellation.getLines()) {
                if (targetConstellation.lineIntersects(line)) {
                    SpyglassAstronomyClient.say(String.format("Cannot add new Constellation: collision with Constellation \"%s\"", targetConstellation.name));
                    return -1;
                }
            }
        }
        SpyglassAstronomyClient.constellations.add(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        return 1;
    }
}
