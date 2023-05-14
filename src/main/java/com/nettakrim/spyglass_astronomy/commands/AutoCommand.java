package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AutoCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> autoNode = ClientCommandManager
                .literal("sga:auto")
                .build();

        LiteralCommandNode<FabricClientCommandSource> generateNode = ClientCommandManager
                .literal("generate")
                .executes(AutoCommand::generate)
                .build();

        LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
                .literal("name")
                .build();

        LiteralCommandNode<FabricClientCommandSource> nameConstellations = ClientCommandManager
                .literal("constellations")
                .executes(AutoCommand::nameConstellations)
                .build();

        LiteralCommandNode<FabricClientCommandSource> nameStars = ClientCommandManager
                .literal("stars")
                .executes(AutoCommand::nameStars)
                .build();

        LiteralCommandNode<FabricClientCommandSource> namePlanets = ClientCommandManager
                .literal("planets")
                .executes(AutoCommand::namePlanets)
                .build();

        autoNode.addChild(generateNode);

        nameNode.addChild(nameConstellations);
        nameNode.addChild(nameStars);
        nameNode.addChild(namePlanets);
        autoNode.addChild(nameNode);

        return autoNode;
    }

    private static int generate(CommandContext<FabricClientCommandSource> context) {
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int nameConstellations(CommandContext<FabricClientCommandSource> context) {
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            if (constellation.isUnnamed()) {
                constellation.name = generateConstellationName();
            }
        }
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int nameStars(CommandContext<FabricClientCommandSource> context) {
        //store such that names are regenerated each load? to reduce having to store every name
        //perhaps also do so for planets etc
        //if so require planet to be spotted if auto named to count towards knowledge
        for (Star star : SpyglassAstronomyClient.stars) {
            if (star.isUnnamed()) {
                star.name = generateStarName();
            }
        }
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int namePlanets(CommandContext<FabricClientCommandSource> context) {
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (orbitingBody.isUnnamed()) {
                orbitingBody.name = generatePlanetName();
            }
        }
        SpaceDataManager.makeChange();
        return 1;
    }

    private static String generateConstellationName() {
        return "";
    }

    private static String generateStarName() {
        return "";
    }

    private static String generatePlanetName() {
        return "";
    }
}
