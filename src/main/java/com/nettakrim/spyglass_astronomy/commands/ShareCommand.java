package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.text.Text;

public class ShareCommand implements Command<FabricClientCommandSource> {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> shareNode = ClientCommandManager
            .literal("sga:share")
            .executes(new ShareCommand())
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationShareNode = ClientCommandManager
            .literal("constellation")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.constellations)
                    .executes(ShareCommand::shareConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> starShareNode = ClientCommandManager
            .literal("star")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.stars)
                    .executes(ShareCommand::shareStar)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyShareNode = ClientCommandManager
            .literal("planet")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.orbitingBodies)
                    .executes(ShareCommand::shareOrbitingBody)
            )
            .build();

        shareNode.addChild(constellationShareNode);
        shareNode.addChild(starShareNode);
        shareNode.addChild(orbitingBodyShareNode);
        return shareNode;
    }

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
        SpyglassAstronomyClient.say("commands.share.nothingselected");
        return -1;
	}

    private static int shareConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        share(constellation);
        return 1;
    }

    private static int shareStar(CommandContext<FabricClientCommandSource> context) {
        Star star = SpyglassAstronomyCommands.getStar(context);
        if (star == null) {
            return -1;
        }
        share(star);
        return 1;
    }

    private static int shareOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        OrbitingBody orbitingBody = SpyglassAstronomyCommands.getOrbitingBody(context);
        if (orbitingBody == null) {
            return -1;
        }
        share(orbitingBody);
        return 1;
    }

    private static void share(Constellation constellation) {
        Text text = SpyglassAstronomyCommands.getClickHere(
            "commands.share.constellation",
            "sga:c_"+(SpaceDataManager.encodeConstellation(null, constellation).replace(" | ", "|"))+"|",
            false,
            constellation.name
        );
        SpyglassAstronomyClient.sayText(text);
    }

    private static void share(Star star) {
        String starName = (star.isUnnamed() ? "Unnamed" : star.name);
        Text text = SpyglassAstronomyCommands.getClickHere(
            "commands.share.star",
            "sga:s_"+starName+"|"+ star.index +"|",
            false,
            starName
        );
        SpyglassAstronomyClient.sayText(text);
    }

    private static void share(OrbitingBody orbitingBody) {
        String orbitingBodyName = (orbitingBody.isUnnamed() ? "Unnamed" : orbitingBody.name);
        Text text = SpyglassAstronomyCommands.getClickHere(
            "commands.share."+(orbitingBody.isPlanet ?"planet" : "comet"),
            "sga:p_"+orbitingBodyName+"|"+ SpyglassAstronomyClient.orbitingBodies.indexOf(orbitingBody) +"|",
            false,
            orbitingBodyName
        );
        SpyglassAstronomyClient.sayText(text);
    }
}
