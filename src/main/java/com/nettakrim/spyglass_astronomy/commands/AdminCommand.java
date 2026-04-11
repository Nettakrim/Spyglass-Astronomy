package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;

import com.nettakrim.spyglass_astronomy.commands.admin_subcommands.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class AdminCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> adminNode = ClientCommands
            .literal("sga:admin")
            .build();

        adminNode.addChild(BypassCommand.getCommandNode());
        adminNode.addChild(ChangesCommand.getCommandNode());
        adminNode.addChild(ConstellationsCommand.getCommandNode());
        adminNode.addChild(RenameCommand.getCommandNode());
        adminNode.addChild(SeedCommand.getCommandNode());
        adminNode.addChild(StarCountCommand.getCommandNode());
        adminNode.addChild(YearLengthCommand.getCommandNode());

        return adminNode;
    }
}
