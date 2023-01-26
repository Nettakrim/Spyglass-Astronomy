package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ShareCommand implements Command<FabricClientCommandSource> {
	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (Constellation.selected != null) {
            share(Constellation.selected);
            return 1;
        }
        if (Star.selected != null) {
            share(Star.selected);
            return 1;
        }
        if (OrbitingBody.selected != null) {
            share(OrbitingBody.selected);
            return 1;
        }
        return -1;
	}

    public static int shareConstellation(CommandContext<FabricClientCommandSource> context) {
        share(SpyglassAstronomyCommands.getConstellation(context));
        return 1;
    }

    public static int shareStar(CommandContext<FabricClientCommandSource> context) {
        share(SpyglassAstronomyCommands.getStar(context));
        return 1;
    }

    public static int shareOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        share(SpyglassAstronomyCommands.getOrbitingBody(context));
        return 1;
    }

    public static void share(Constellation constellation) {
        String translate = "[Spyglass Astronomy] "+Text.translatable(SpyglassAstronomyClient.MODID+".commands.share.constellation").toString();
        Text text = SpyglassAstronomyCommands.getClickHere(
            String.format(translate, constellation.name),
            "sga:c_"+(SpaceDataManager.encodeConstellation(null, constellation).replace(" | ", "|"))+"|",
            false
        );
        SpyglassAstronomyClient.say(text, false);
    }

    public static void share(Star star) {
        String translate = "[Spyglass Astronomy] "+Text.translatable(SpyglassAstronomyClient.MODID+".commands.share.star").toString();
        String starName = (star.name == null ? "Unnamed" : star.name);
        Text text = SpyglassAstronomyCommands.getClickHere(
            String.format(translate, starName),
            "sga:s_"+starName+"|"+Integer.toString(star.index)+"|",
            false
        );
        SpyglassAstronomyClient.say(text, false);
    }

    public static void share(OrbitingBody orbitingBody) {
        String translate = "[Spyglass Astronomy] "+Text.translatable(SpyglassAstronomyClient.MODID+".commands.share."+(orbitingBody.isPlanet ?"planet" : "comet")).toString();
        String orbitingBodyName = (orbitingBody.name == null ? "Unnamed" : orbitingBody.name);
        Text text = SpyglassAstronomyCommands.getClickHere(
            String.format(translate, orbitingBody),
            "sga:p_"+orbitingBodyName+"|"+Integer.toString(SpyglassAstronomyClient.orbitingBodies.indexOf(orbitingBody))+"|",
            false
        );
        SpyglassAstronomyClient.say(text, false);
    }
}
