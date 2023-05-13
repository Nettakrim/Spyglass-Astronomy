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
            SpyglassAstronomyClient.say("commands.select.constellation.fail");
            return -1;
        }
        constellation.select();
        SpyglassAstronomyClient.say("commands.select.constellation", constellation.name);
        return 1;
    }

    public static int selectStar(CommandContext<FabricClientCommandSource> context) {
        Star star = SpyglassAstronomyCommands.getStar(context);
        if (star == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.isHoldingSpyglass()) {
            SpyglassAstronomyClient.say("commands.select.star.fail");
            return -1;
        }
        star.select();
        String starName = (star.isUnnamed() ? "Unnamed" : star.name);
        SpyglassAstronomyClient.say("commands.select.star", starName);
        return 1;
    }

    public static int selectOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        OrbitingBody orbitingBody = SpyglassAstronomyCommands.getOrbitingBody(context);
        if (orbitingBody == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.isHoldingSpyglass()) {
            SpyglassAstronomyClient.say("commands.select."+(orbitingBody.isPlanet ? "planet" : "comet")+".fail");
            return -1;
        }
        orbitingBody.select();
        String orbitingBodyName = (orbitingBody.isUnnamed() ? "Unnamed" : orbitingBody.name);
        SpyglassAstronomyClient.say("commands.select."+(orbitingBody.isPlanet ? "planet" : "comet"), orbitingBodyName);
        return 1;
    }
}
