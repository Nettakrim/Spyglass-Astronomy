package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.context.CommandContext;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SelectCommand {
    public static int selectConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.isHoldingSpyglass()) {
            SpyglassAstronomyClient.say("commands.select.constellation");
            return -1;
        }
        constellation.select();
        return 1;
    }

    public static int selectStar(CommandContext<FabricClientCommandSource> context) {
        Star star = SpyglassAstronomyCommands.getStar(context);
        if (star == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.isHoldingSpyglass()) {
            SpyglassAstronomyClient.say("commands.select.star");
            return -1;
        }
        star.select();
        return 1;
    }

    public static int selectOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        OrbitingBody orbitingBody = SpyglassAstronomyCommands.getOrbitingBody(context);
        if (orbitingBody == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.isHoldingSpyglass()) {
            SpyglassAstronomyClient.say("commands.select."+(orbitingBody.isPlanet ? "planet" : "comet"));
            return -1;
        }
        orbitingBody.select();
        return 1;
    }
}
