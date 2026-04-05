package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class HideCommand implements Command<FabricClientCommandSource> {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> hideNode = ClientCommands
            .literal("sga:hide")
            .executes(new HideCommand())
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationsHideNode = ClientCommands
            .literal("constellations")
            .executes(HideCommand::hideConstellations)
            .build();

        LiteralCommandNode<FabricClientCommandSource> starsHideNode = ClientCommands
            .literal("stars")
            .executes(HideCommand::hideStars)
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodiesHideNode = ClientCommands
            .literal("planets")
            .executes(HideCommand::hideOrbitingBodies)
            .build();

        LiteralCommandNode<FabricClientCommandSource> oldStarsHideNode = ClientCommands
            .literal("vanillastars")
            .executes(HideCommand::hideOldStars)
            .build();

        LiteralCommandNode<FabricClientCommandSource> dayTimeHideNode = ClientCommands
            .literal("daytime")
            .executes(HideCommand::hideDaytime)
            .build();

        hideNode.addChild(constellationsHideNode);
        hideNode.addChild(starsHideNode);
        hideNode.addChild(orbitingBodiesHideNode);
        hideNode.addChild(oldStarsHideNode);
        hideNode.addChild(dayTimeHideNode);

        return hideNode;
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        boolean active = !(SpaceRenderingManager.constellationsVisible || SpaceRenderingManager.starsVisible || SpaceRenderingManager.orbitingBodiesVisible || SpaceRenderingManager.oldStarsVisible);
        SpaceRenderingManager.constellationsVisible = active;
        SpaceRenderingManager.starsVisible = active;
        SpaceRenderingManager.orbitingBodiesVisible = active;
        SpaceRenderingManager.oldStarsVisible = false;
        sayHideUpdate("all", active);
        return 1;
	}

    private static int hideConstellations(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.constellationsVisible = !SpaceRenderingManager.constellationsVisible;
        sayHideUpdate("constellations", SpaceRenderingManager.constellationsVisible);
        return 1;
    }

    private static int hideStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.starsVisible = !SpaceRenderingManager.starsVisible;
        sayHideUpdate("stars", SpaceRenderingManager.starsVisible);
        return 1;
    }

    private static int hideOrbitingBodies(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.orbitingBodiesVisible = !SpaceRenderingManager.orbitingBodiesVisible;
        sayHideUpdate("planets", SpaceRenderingManager.orbitingBodiesVisible);
        return 1;
    }

    private static int hideOldStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.oldStarsVisible = !SpaceRenderingManager.oldStarsVisible;
        sayHideUpdate("vanillastars", SpaceRenderingManager.oldStarsVisible);
        return 1;
    }

    private static int hideDaytime(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.starsAlwaysVisible = !SpaceRenderingManager.starsAlwaysVisible;
        sayHideUpdate("daytime", SpaceRenderingManager.starsAlwaysVisible);
        return 1;
    }


    private static void sayHideUpdate(String base, boolean active) {
        SpyglassAstronomyClient.say("commands.hide."+base+(active ? ".show" :".hide"));
    }
}
