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
        boolean active = SpaceRenderingManager.constellationsVisible || SpaceRenderingManager.starsVisible || SpaceRenderingManager.orbitingBodiesVisible || SpaceRenderingManager.oldStarsVisible;
        SpaceRenderingManager.constellationsVisible = !active;
        SpaceRenderingManager.starsVisible = !active;
        SpaceRenderingManager.orbitingBodiesVisible = !active;
        SpaceRenderingManager.oldStarsVisible = false;
        if (active) SpyglassAstronomyClient.say("All hidden, run /sga:hide again to unhide");
        return 1;
	}

    public static int hideConstellations(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.constellationsVisible = !SpaceRenderingManager.constellationsVisible;
        if (!SpaceRenderingManager.constellationsVisible) SpyglassAstronomyClient.say("Constellations hidden, run /sga:hide constellations again to unhide");
        return 1;
    }

    public static int hideStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.starsVisible = !SpaceRenderingManager.starsVisible;
        if (!SpaceRenderingManager.starsVisible) SpyglassAstronomyClient.say("Stars hidden, run /sga:hide stars again to unhide");
        return 1;
    }

    public static int hideOrbitingBodies(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.orbitingBodiesVisible = !SpaceRenderingManager.orbitingBodiesVisible;
        if (!SpaceRenderingManager.orbitingBodiesVisible) SpyglassAstronomyClient.say("Planets hidden, run /sga:hide planets again to unhide");
        return 1;
    }

    public static int hideOldStars(CommandContext<FabricClientCommandSource> context) {
        SpaceRenderingManager.oldStarsVisible = !SpaceRenderingManager.oldStarsVisible;
        if (SpaceRenderingManager.oldStarsVisible) SpyglassAstronomyClient.say("Vanilla Stars unhidden, run /sga:hide vanillastars again to hide");
        return 1;
    }

}
