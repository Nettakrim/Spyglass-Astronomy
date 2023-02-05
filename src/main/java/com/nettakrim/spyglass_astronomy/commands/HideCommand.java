package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class HideCommand implements Command<FabricClientCommandSource> {
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

    public static int hideConstellations(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.constellationsVisible = !SpaceRenderingManager.constellationsVisible;
        sayHideUpdate("constellations", SpaceRenderingManager.constellationsVisible);
        return 1;
    }

    public static int hideStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.starsVisible = !SpaceRenderingManager.starsVisible;
        sayHideUpdate("stars", SpaceRenderingManager.starsVisible);
        return 1;
    }

    public static int hideOrbitingBodies(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.orbitingBodiesVisible = !SpaceRenderingManager.orbitingBodiesVisible;
        sayHideUpdate("planets", SpaceRenderingManager.orbitingBodiesVisible);
        return 1;
    }

    public static int hideOldStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.oldStarsVisible = !SpaceRenderingManager.oldStarsVisible;
        sayHideUpdate("vanillastars", SpaceRenderingManager.oldStarsVisible);
        return 1;
    }

    private static void sayHideUpdate(String base, boolean active) {
        SpyglassAstronomyClient.say("commands.hide."+base+(active ? ".show" :".hide"));
    }
}
