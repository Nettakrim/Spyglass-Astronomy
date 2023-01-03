package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AdminCommand {
    public static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        IntegerArgumentType.getInteger(context, "amount");
        return 1;
    }

    public static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        LongArgumentType.getLong(context, "seed");
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
}
