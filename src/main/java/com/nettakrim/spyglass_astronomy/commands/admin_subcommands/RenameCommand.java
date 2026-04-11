package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.commands.NameCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.MessageArgument;

public class RenameCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> renameNode = ClientCommands
            .literal("rename")
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationNameNode = ClientCommands
            .literal("constellation")
            .then(
                ClientCommands.argument("index", IntegerArgumentType.integer(0))
                    .then(
                        ClientCommands.argument("name", MessageArgument.message())
                            .executes(NameCommand::nameConstellation)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> starNameNode = ClientCommands
            .literal("star")
            .then(
                ClientCommands.argument("index", IntegerArgumentType.integer(0))
                    .then(
                        ClientCommands.argument("name", MessageArgument.message())
                            .executes(NameCommand::nameStar)
                    )
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyNameNode = ClientCommands
            .literal("planet")
            .then(
                ClientCommands.argument("index", IntegerArgumentType.integer(0))
                    .then(
                        ClientCommands.argument("name", MessageArgument.message())
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
