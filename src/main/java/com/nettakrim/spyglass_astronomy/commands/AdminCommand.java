package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.StarLine;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;

public class AdminCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> adminNode = ClientCommandManager
            .literal("sga:admin")
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("removeconstellation")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.constellations)
                    .executes(AdminCommand::removeConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> setStarCountNode = ClientCommandManager
            .literal("setstarcount")
            .then(
                ClientCommandManager.argument("amount", IntegerArgumentType.integer(0,4095))
                    .executes(AdminCommand::setStarCount)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> bypassNode = ClientCommandManager
            .literal("bypassknowledge")
            .executes(AdminCommand::bypassKnowledge)
            .build();

        LiteralCommandNode<FabricClientCommandSource> yearLengthNode = ClientCommandManager
            .literal("setyearlength")
            .then(
                ClientCommandManager.argument("days", FloatArgumentType.floatArg(1f/8f))
                    .executes(AdminCommand::setYearLength)
            )
            .build();

        adminNode.addChild(removeNode);
        adminNode.addChild(setStarCountNode);
        adminNode.addChild(bypassNode);
        adminNode.addChild(yearLengthNode);

        adminNode.addChild(getAddNode());
        adminNode.addChild(getSetSeedNode());
        adminNode.addChild(getRenameNode());
        adminNode.addChild(getChangesNode());

        return adminNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getAddNode() {
        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .build();

        LiteralCommandNode<FabricClientCommandSource> addConstellationNode = ClientCommandManager
            .literal("constellation")
            .then(
                ClientCommandManager.argument("data", MessageArgumentType.message())
                    .executes(AdminCommand::addConstellation)
            )
            .build();

        addNode.addChild(addConstellationNode);
        return addNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getSetSeedNode() {
        LiteralCommandNode<FabricClientCommandSource> setSeedNode = ClientCommandManager
            .literal("setseed")
            .build();

        LiteralCommandNode<FabricClientCommandSource> setStarSeedNode = ClientCommandManager
            .literal("star")
            .then(
                ClientCommandManager.argument("seed", LongArgumentType.longArg())
                    .executes(AdminCommand::setStarSeed)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> setPlanetSeedNode = ClientCommandManager
            .literal("planet")
            .then(
                ClientCommandManager.argument("seed", LongArgumentType.longArg())
                    .executes(AdminCommand::setPlanetSeed)
            )
            .build();

        setSeedNode.addChild(setStarSeedNode);
        setSeedNode.addChild(setPlanetSeedNode);
        return setSeedNode;
    }


    private static LiteralCommandNode<FabricClientCommandSource> getRenameNode() {
        LiteralCommandNode<FabricClientCommandSource> renameNode = ClientCommandManager
            .literal("rename")
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationNameNode = ClientCommandManager
            .literal("constellation")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer())
                    .then(
                        ClientCommandManager.argument("name", MessageArgumentType.message())
                            .executes(NameCommand::nameConstellation)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> starNameNode = ClientCommandManager
            .literal("star")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer())
                    .then(
                        ClientCommandManager.argument("name", MessageArgumentType.message())
                            .executes(NameCommand::nameStar)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyNameNode = ClientCommandManager
            .literal("planet")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer())
                    .then(
                        ClientCommandManager.argument("name", MessageArgumentType.message())
                            .executes(NameCommand::nameOrbitingBody)
                    )
            )
            .build();

        renameNode.addChild(constellationNameNode);
        renameNode.addChild(starNameNode);
        renameNode.addChild(orbitingBodyNameNode);
        return renameNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getChangesNode() {
        LiteralCommandNode<FabricClientCommandSource> changesNode = ClientCommandManager
            .literal("changes")
            .build();

        LiteralCommandNode<FabricClientCommandSource> discardNode = ClientCommandManager
            .literal("discard")
            .executes(AdminCommand::discardUnsavedChanges)
            .build();

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
            .literal("save")
            .executes(AdminCommand::saveChanges)
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
            .literal("query")
            .executes(AdminCommand::queryChanges)
            .build();

        changesNode.addChild(discardNode);
        changesNode.addChild(saveNode);
        changesNode.addChild(queryNode);
        return changesNode;
    }

    private static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        SpyglassAstronomyClient.say("commands.admin.setstarcount", Integer.toString(amount), Integer.toString(SpyglassAstronomyClient.getStarCount()));
        SpyglassAstronomyClient.setStarCount(amount);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setstarseed", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getStarSeed()));
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(seed);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setplanetseed", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getPlanetSeed()));
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(seed);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int removeConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        SpyglassAstronomyClient.say("commands.admin.removeconstellation", constellation.name);
        SpyglassAstronomyClient.constellations.remove(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int saveChanges(CommandContext<FabricClientCommandSource> context) {
        int changes = SpaceDataManager.getChanges();
        if (changes != 0) {
            SpyglassAstronomyClient.say("commands.admin.changes.save", Integer.toString(changes));
        } else {
            SpyglassAstronomyClient.say("commands.admin.changes.save.none");
        }
        SpyglassAstronomyClient.saveSpace();
        return 1;
    }

    private static int discardUnsavedChanges(CommandContext<FabricClientCommandSource> context) {
        int changes = SpaceDataManager.getChanges();
        if (changes != 0) {
            SpyglassAstronomyClient.say("commands.admin.changes.discard", Integer.toString(changes));
        } else {
            SpyglassAstronomyClient.say("commands.admin.changes.discard.none");
        }
        SpyglassAstronomyClient.discardUnsavedChanges();
        return 1;
    }

    private static int queryChanges(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.changes.query", Integer.toString(SpaceDataManager.getChanges()));
        return 1;
    }

    private static int addConstellation(CommandContext<FabricClientCommandSource> context) {
        String dataRaw = SpyglassAstronomyCommands.getMessageText(context,"data");
        int index = dataRaw.indexOf(' ');
        Constellation constellation = SpaceDataManager.decodeConstellation(null, dataRaw.substring(index+1), dataRaw.substring(0, index));
        constellation.initaliseStarLines();
        for (Constellation targetConstellation : SpyglassAstronomyClient.constellations) {
            for (StarLine line : constellation.getLines()) {
                if (targetConstellation.lineIntersects(line)) {
                    SpyglassAstronomyClient.say("commands.admin.addconstellation.fail", constellation.name, targetConstellation.name);
                    return -1;
                }
            }
        }
        SpyglassAstronomyClient.say("commands.admin.addconstellation", constellation.name);
        constellation.select();
        SpyglassAstronomyClient.constellations.add(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int bypassKnowledge(CommandContext<FabricClientCommandSource> context) {
        if (SpyglassAstronomyClient.knowledge.bypassKnowledge()) {
            SpyglassAstronomyClient.say("commands.admin.bypass.on");    
        } else {
            SpyglassAstronomyClient.say("commands.admin.bypass.off");
        }
        return 1;
    }

    public static int setYearLength(CommandContext<FabricClientCommandSource> context) {
        float yearLength = FloatArgumentType.getFloat(context, "days");
        SpyglassAstronomyClient.say("commands.admin.setyearlength", Float.toString(yearLength), Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        SpyglassAstronomyClient.spaceDataManager.setYearLength(yearLength);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }
}
