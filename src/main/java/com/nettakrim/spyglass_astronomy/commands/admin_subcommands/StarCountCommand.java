package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpaceDataManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.StarLine;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.ArrayList;

public class StarCountCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> starCountNode = ClientCommandManager
            .literal("starcount")
            .build();

        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
            .literal("query")
            .executes(StarCountCommand::queryStarCount)
            .build();

        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
            .literal("reset")
            .executes(StarCountCommand::resetStarCount)
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .then(
                ClientCommandManager.argument("amount", IntegerArgumentType.integer(0,4095))
                    .executes(StarCountCommand::setStarCount)
            )
            .build();

        starCountNode.addChild(queryNode);
        starCountNode.addChild(resetNode);
        starCountNode.addChild(setNode);
        return starCountNode;
    }

    public static final ArrayList<Constellation> invalidatedConstellations = new ArrayList<>();

    private static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        return setStarCount(IntegerArgumentType.getInteger(context, "amount"));
    }

    private static int resetStarCount(CommandContext<FabricClientCommandSource> context) {
        return setStarCount(1024);
    }

    private static int setStarCount(int amount) {
        boolean reducedStars = amount < SpyglassAstronomyClient.getStarCount();
        SpyglassAstronomyClient.say("commands.admin.starcount.set", Integer.toString(amount), Integer.toString(SpyglassAstronomyClient.getStarCount()));
        SpyglassAstronomyClient.setStarCount(amount);
        SpyglassAstronomyClient.generateStars(null, true, false);

        if (reducedStars) {
            ArrayList<Constellation> validConstellations = new ArrayList<>(SpyglassAstronomyClient.constellations.size());
            for (Constellation constellation : SpyglassAstronomyClient.constellations) {
                boolean valid = true;
                for (StarLine line : constellation.getLines()) {
                    int star = line.getOtherStar(-1);
                    int max = Math.max(line.getOtherStar(star), star);
                    if (max >= amount) {
                        invalidatedConstellations.add(constellation);
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    validConstellations.add(constellation);
                }
            }
            int difference = SpyglassAstronomyClient.constellations.size()-validConstellations.size();
            if (difference != 0) {
                SpyglassAstronomyClient.say("commands.admin.starcount.set.invalidate", difference);
            }
            SpyglassAstronomyClient.constellations.clear();
            for (Constellation constellation : validConstellations) {
                ConstellationsCommand.addConstellation(constellation, false, false);
            }
            SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        } else {
            ArrayList<Constellation> validConstellations = new ArrayList<>(invalidatedConstellations.size());
            for (Constellation constellation : invalidatedConstellations) {
                boolean valid = true;
                for (StarLine line : constellation.getLines()) {
                    int star = line.getOtherStar(-1);
                    int max = Math.max(line.getOtherStar(star), star);
                    if (max >= amount) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    validConstellations.add(constellation);
                }
            }
            if (validConstellations.size() != 0) {
                int validated = 0;
                for (Constellation constellation : validConstellations) {
                    if (ConstellationsCommand.addConstellation(constellation, false, false) == 1) {
                        invalidatedConstellations.remove(constellation);
                        validated++;
                    }
                }
                if (validated > 0) {
                    SpyglassAstronomyClient.say("commands.admin.starcount.set.validate", validated);
                    SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
                }
            }
        }

        SpaceDataManager.makeChange();
        return 1;
    }

    private static int queryStarCount(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.starcount.query", Integer.toString(SpyglassAstronomyClient.getStarCount()));
        return 1;
    }
}
