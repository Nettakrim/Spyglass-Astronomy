package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class YearLengthCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> yearLengthNode = ClientCommandManager
            .literal("yearlength")
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
            .literal("query")
            .executes(YearLengthCommand::queryYearLength)
            .build();

        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
            .literal("reset")
            .executes(YearLengthCommand::resetYearLength)
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .then(
                ClientCommandManager.argument("days", FloatArgumentType.floatArg(1f/8f))
                    .executes(YearLengthCommand::setYearLength)
            )
            .build();

        yearLengthNode.addChild(queryNode);
        yearLengthNode.addChild(resetNode);
        yearLengthNode.addChild(setNode);
        return yearLengthNode;
    }

    private static int setYearLength(CommandContext<FabricClientCommandSource> context) {
        return setYearLength(FloatArgumentType.getFloat(context, "days"));
    }

    private static int resetYearLength(CommandContext<FabricClientCommandSource> context) {
        return setYearLength(8f);
    }

    public static int setYearLength(float yearLength) {
        SpyglassAstronomyClient.say("commands.admin.yearlength.set", Float.toString(yearLength), Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        SpyglassAstronomyClient.spaceDataManager.setYearLength(yearLength);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int queryYearLength(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.yearlength.query", Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        return 1;
    }
}
