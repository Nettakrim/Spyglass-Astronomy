package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class BypassCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> bypassNode = ClientCommandManager
            .literal("bypassknowledge")
            .executes(BypassCommand::bypassKnowledge)
            .build();

        return bypassNode;
    }

    public static int bypassKnowledge(CommandContext<FabricClientCommandSource> context) {
        if (SpyglassAstronomyClient.knowledge.bypassKnowledge()) {
            SpyglassAstronomyClient.say("commands.admin.bypass.on");
        } else {
            SpyglassAstronomyClient.say("commands.admin.bypass.off");
        }
        return 1;
    }
}
