package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.commands.NameCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;

public class RenameCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> renameNode = ClientCommandManager
            .literal("rename")
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationNameNode = ClientCommandManager
            .literal("constellation")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                    .then(
                        ClientCommandManager.argument("name", MessageArgumentType.message())
                            .executes(NameCommand::nameConstellation)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> starNameNode = ClientCommandManager
            .literal("star")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
                    .then(
                        ClientCommandManager.argument("name", MessageArgumentType.message())
                            .executes(NameCommand::nameStar)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyNameNode = ClientCommandManager
            .literal("planet")
            .then(
                ClientCommandManager.argument("index", IntegerArgumentType.integer(0))
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
}
