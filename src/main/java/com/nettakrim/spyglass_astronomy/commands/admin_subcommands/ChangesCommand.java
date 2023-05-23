package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ChangesCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> changesNode = ClientCommandManager
            .literal("changes")
            .build();

        LiteralCommandNode<FabricClientCommandSource> discardNode = ClientCommandManager
            .literal("discard")
            .executes(ChangesCommand::discardUnsavedChanges)
            .build();

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
            .literal("save")
            .executes(ChangesCommand::saveChanges)
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
            .literal("query")
            .executes(ChangesCommand::queryChanges)
            .build();

        changesNode.addChild(discardNode);
        changesNode.addChild(saveNode);
        changesNode.addChild(queryNode);
        return changesNode;
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
}
