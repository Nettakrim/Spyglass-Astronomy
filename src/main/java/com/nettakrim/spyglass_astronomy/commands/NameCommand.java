package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class NameCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String name = SpyglassAstronomyCommands.getMessageText(context);
        name = name.replace("|", "");
        name = name.replaceAll("^ +| +$|( )+", "$1"); //remove double spaces
        if (Constellation.selected != null) {
            name(Constellation.selected, name);
            return 1;
        }
        if (Star.selected != null) {
            name(Star.selected, name);
            return 1;
        }
        if (OrbitingBody.selected != null) {
            name(OrbitingBody.selected, name);
            return 1;
        }
        SpyglassAstronomyClient.say("commands.name.nothingselected");
        return -1;
	}

    public static int nameConstellation(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = SpyglassAstronomyCommands.getMessageText(context);
        name(SpyglassAstronomyClient.constellations.get(index), name);
        return 1;
    }

    public static int nameStar(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = SpyglassAstronomyCommands.getMessageText(context);
        name(SpyglassAstronomyClient.stars.get(index), name);
        return 1;
    }

    public static int nameOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = SpyglassAstronomyCommands.getMessageText(context);
        name(SpyglassAstronomyClient.orbitingBodies.get(index), name);
        return 1;
    }

    public static void name(Constellation constellation, String name) {
        if (constellation.isUnnamed()) {
            SpyglassAstronomyClient.say("commands.name.constellation", name);
        } else {
            SpyglassAstronomyClient.say("commands.name.constellation.rename", constellation.name, name);
        }
        constellation.name = name;
        constellation.select();
        SpaceDataManager.makeChange();
    }

    public static void name(Star star, String name) {
        if (star.isUnnamed()) {
            SpyglassAstronomyClient.say("commands.name.star", name);
        } else {
            SpyglassAstronomyClient.say("commands.name.star.rename", star.name, name);
        }
        star.name = name;
        star.select();
        SpaceDataManager.makeChange();
    }

    public static void name(OrbitingBody orbitingBody, String name) {
        if (orbitingBody.isUnnamed()) {
            SpyglassAstronomyClient.say("commands.name."+(orbitingBody.isPlanet ? "planet" : "comet"), name);
        } else {
            SpyglassAstronomyClient.say("commands.name."+(orbitingBody.isPlanet ? "planet" : "comet")+".rename", orbitingBody.name, name);
        }
        orbitingBody.name = name;
        orbitingBody.select();
        SpaceDataManager.makeChange();
    }
}
