package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
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
        int amount = IntegerArgumentType.getInteger(context, "amount");
        SpyglassAstronomyClient.say("commands.admin.setstarcount", Integer.toString(amount), Integer.toString(SpyglassAstronomyClient.getStarCount()));
        SpyglassAstronomyClient.setStarCount(amount);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        Long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setstarseed", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getStarSeed()));
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(seed);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        Long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setplanetseed", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getPlanetSeed()));
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(seed);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int removeConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        SpyglassAstronomyClient.say("commands.admin.removeconstellation", constellation.name);
        SpyglassAstronomyClient.constellations.remove(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int saveChanges(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.changes.save", Integer.toString(SpaceDataManager.getChanges()));
        SpyglassAstronomyClient.saveSpace();
        return 1;
    }

    public static int discardUnsavedChanges(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.changes.discard", Integer.toString(SpaceDataManager.getChanges()));
        SpyglassAstronomyClient.discardUnsavedChanges();
        return 1;
    }

    public static int queryChanges(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.changes.query", Integer.toString(SpaceDataManager.getChanges()));
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
                    SpyglassAstronomyClient.say("commands.admin.addconstellation.fail", constellation.name, targetConstellation.name);
                    return -1;
                }
            }
        }
        SpyglassAstronomyClient.say("commands.admin.addconstellation", constellation.name);
        constellation.select();
        SpyglassAstronomyClient.constellations.add(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int bypassKnowledge(CommandContext<FabricClientCommandSource> context) {
        if (SpyglassAstronomyClient.knowledge.bypassKnowledge()) {
            SpyglassAstronomyClient.say("commands.admin.bypass.on");    
        } else {
            SpyglassAstronomyClient.say("commands.admin.bypass.off");
        }
        return 1;
    }

    public static int setYearLength(CommandContext<FabricClientCommandSource> context) {
        float yearLength = FloatArgumentType.getFloat(context, "days");
        SpyglassAstronomyClient.say("commands.admin.setyearlength", Float.toString(yearLength), Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        SpyglassAstronomyClient.spaceDataManager.setYearLength(yearLength);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }
}
