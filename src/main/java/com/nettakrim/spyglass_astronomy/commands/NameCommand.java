package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
        SpaceDataManager.makeChange();

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
        return -1;
	}

    public static int nameConstellation(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = StringArgumentType.getString(context, "name");
        name(SpyglassAstronomyClient.constellations.get(index), name);
        return 1;
    }

    public static int nameStar(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = StringArgumentType.getString(context, "name");
        name(SpyglassAstronomyClient.stars.get(index), name);
        return 1;
    }

    public static int nameOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        int index = IntegerArgumentType.getInteger(context, "index");
        String name = StringArgumentType.getString(context, "name");
        name(SpyglassAstronomyClient.orbitingBodies.get(index), name);
        return 1;
    }

    public static void name(Constellation constellation, String name) {
        if (Constellation.selected.name.equals("Unnamed")) {
            SpyglassAstronomyClient.say(String.format("Named new Constellation \"%s\"", name));
        } else {
            SpyglassAstronomyClient.say(String.format("Renamed Constellation \"%s\" to \"%s\"", Constellation.selected.name, name));
        }

        Constellation.selected.name = name;
    }

    public static void name(Star star, String name) {
        if (Star.selected.name == null) {
            SpyglassAstronomyClient.say(String.format("Named Star \"%s\"", name));
        } else {
            SpyglassAstronomyClient.say(String.format("Renamed Star \"%s\" to \"%s\"", Star.selected.name, name));
        }

        Star.selected.name = name;
    }

    public static void name(OrbitingBody orbitingBody, String name) {
        if (OrbitingBody.selected.name == null) {
            SpyglassAstronomyClient.say(String.format("Named Planet \"%s\"", name));
        } else {
            SpyglassAstronomyClient.say(String.format("Renamed Planet \"%s\" to \"%s\"", OrbitingBody.selected.name, name));
        }

        OrbitingBody.selected.name = name;
    }
}
