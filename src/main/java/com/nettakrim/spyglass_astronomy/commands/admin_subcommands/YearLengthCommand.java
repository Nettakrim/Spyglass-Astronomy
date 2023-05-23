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

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .then(
                ClientCommandManager.argument("days", FloatArgumentType.floatArg(1f/8f))
                    .executes(YearLengthCommand::setYearLength)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
            .literal("query")
            .executes(YearLengthCommand::queryYearLength)
            .build();

        yearLengthNode.addChild(setNode);
        yearLengthNode.addChild(queryNode);
        return yearLengthNode;
    }

    public static int setYearLength(CommandContext<FabricClientCommandSource> context) {
        float yearLength = FloatArgumentType.getFloat(context, "days");
        SpyglassAstronomyClient.say("commands.admin.yearlength.set", Float.toString(yearLength), Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        SpyglassAstronomyClient.spaceDataManager.setYearLength(yearLength);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    public static int queryYearLength(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.yearlength.query", Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        return 1;
    }
}
