package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.mixin.BiomeAccessAccessor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SeedCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> seedNode = ClientCommandManager
            .literal("seed")
            .build();


        LiteralCommandNode<FabricClientCommandSource> starNode = ClientCommandManager
            .literal("star")
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryStarSeedNode = ClientCommandManager
            .literal("query")
            .executes(SeedCommand::queryStarSeed)
            .build();

        LiteralCommandNode<FabricClientCommandSource> resetStarSeedNode = ClientCommandManager
            .literal("reset")
            .executes(SeedCommand::resetStarSeed)
            .build();

        LiteralCommandNode<FabricClientCommandSource> setStarSeedNode = ClientCommandManager
            .literal("set")
            .then(
                ClientCommandManager.argument("seed", LongArgumentType.longArg())
                    .executes(SeedCommand::setStarSeed)
            )
            .build();


        LiteralCommandNode<FabricClientCommandSource> planetNode = ClientCommandManager
            .literal("planet")
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryPlanetSeedNode = ClientCommandManager
            .literal("query")
            .executes(SeedCommand::queryPlanetSeed)
            .build();

        LiteralCommandNode<FabricClientCommandSource> resetPlanetSeedNode = ClientCommandManager
            .literal("reset")
            .executes(SeedCommand::resetPlanetSeed)
            .build();

        LiteralCommandNode<FabricClientCommandSource> setPlanetSeedNode = ClientCommandManager
            .literal("set")
            .then(
                ClientCommandManager.argument("seed", LongArgumentType.longArg())
                    .executes(SeedCommand::setPlanetSeed)
            )
            .build();

        starNode.addChild(queryStarSeedNode);
        starNode.addChild(resetStarSeedNode);
        starNode.addChild(setStarSeedNode);
        seedNode.addChild(starNode);

        planetNode.addChild(queryPlanetSeedNode);
        planetNode.addChild(resetPlanetSeedNode);
        planetNode.addChild(setPlanetSeedNode);
        seedNode.addChild(planetNode);
        return seedNode;
    }

    private static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        return setStarSeed(LongArgumentType.getLong(context, "seed"));
    }

    private static int resetStarSeed(CommandContext<FabricClientCommandSource> context) {
        return setStarSeed(((BiomeAccessAccessor)SpyglassAstronomyClient.world.getBiomeAccess()).getSeed());
    }

    private static int queryStarSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.seed.star.query", Long.toString(SpyglassAstronomyClient.spaceDataManager.getStarSeed()));
        return 1;
    }

    private static int setStarSeed(long seed) {
        SpyglassAstronomyClient.say("commands.admin.seed.star.set", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getStarSeed()));
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(seed);
        SpyglassAstronomyClient.generateStars(null, true, true);
        StarCountCommand.invalidatedConstellations.clear();
        SpaceDataManager.makeChange();
        return 1;
    }



    private static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        return setPlanetSeed(LongArgumentType.getLong(context, "seed"));
    }

    private static int resetPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        return setPlanetSeed(((BiomeAccessAccessor)SpyglassAstronomyClient.world.getBiomeAccess()).getSeed());
    }


    private static int queryPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.seed.planet.query", Long.toString(SpyglassAstronomyClient.spaceDataManager.getPlanetSeed()));
        return 1;
    }

    private static int setPlanetSeed(long seed) {
        SpyglassAstronomyClient.say("commands.admin.seed.planet.set", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getPlanetSeed()));
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(seed);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }
}
