package com.nettakrim.spyglass_astronomy.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.MessageArgumentType.MessageFormat;

public class SpyglassAstronomyCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            SuggestionProvider<FabricClientCommandSource> constellations = (context, builder) -> {
                for (Constellation constellation : SpyglassAstronomyClient.constellations) {
                    builder.suggest(constellation.name);
                }
                return CompletableFuture.completedFuture(builder.build());
            };

            SuggestionProvider<FabricClientCommandSource> stars = (context, builder) -> {
                for (Star star : SpyglassAstronomyClient.stars) {
                    if (star.name != null) builder.suggest(star.name);
                }
                return CompletableFuture.completedFuture(builder.build());
            };

            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
                .literal("sga:info")
                .executes(new InfoCommand())
                .build();

            LiteralCommandNode<FabricClientCommandSource> constellationInfoNode = ClientCommandManager
                .literal("constellation")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(constellations)
                    .executes(InfoCommand::getConstellationInfo)
                )
                .build();

            LiteralCommandNode<FabricClientCommandSource> starInfoNode = ClientCommandManager
                .literal("star")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(stars)
                    .executes(InfoCommand::getStarInfo)
                )
                .build();
            
            dispatcher.getRoot().addChild(infoNode);
            infoNode.addChild(constellationInfoNode);
            infoNode.addChild(starInfoNode);
                
            LiteralCommandNode<FabricClientCommandSource> selectNode = ClientCommandManager
                .literal("sga:select")
                .executes(new InfoCommand())
                .build();

            LiteralCommandNode<FabricClientCommandSource> constellationSelectNode = ClientCommandManager
                .literal("constellation")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(constellations)
                    .executes(SelectCommand::selectConstellation)
                )
                .build();

            LiteralCommandNode<FabricClientCommandSource> starSelectNode = ClientCommandManager
                .literal("star")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(stars)
                    .executes(SelectCommand::selectStar)
                )
                .build();            

            root.addChild(selectNode);
            selectNode.addChild(constellationSelectNode);
            selectNode.addChild(starSelectNode);      

            LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
                .literal("sga:name")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .executes(new NameSelectedCommand())
                )
                .build();

            root.addChild(nameNode);

            LiteralCommandNode<FabricClientCommandSource> adminNode = ClientCommandManager
                .literal("sga:admin")
                .build();

            LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(constellations)
                    .executes(AdminCommand::removeConstellation)
                )
                .build();

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

            LiteralCommandNode<FabricClientCommandSource> setStarCountNode = ClientCommandManager
                .literal("setstarcount")
                .then(
                    ClientCommandManager.argument("amount", IntegerArgumentType.integer(0,4096))
                    .executes(AdminCommand::setStarCount)
                )
                .build();      

            root.addChild(adminNode);
            adminNode.addChild(removeNode);
            adminNode.addChild(setSeedNode);
            setSeedNode.addChild(setStarSeedNode);
            adminNode.addChild(setStarCountNode);
        });
    }

    public static Constellation getConstellation(CommandContext<FabricClientCommandSource> context) {
        String name = getMessageText(context);
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            if (constellation.name.equals(name)) {
                return constellation;
            }
        }
        SpyglassAstronomyClient.say(String.format("No Constellation with name \"%s\" could be found", name));
        return null;        
    }

    public static Star getStar(CommandContext<FabricClientCommandSource> context) {
        String name = getMessageText(context);
        for (Star star : SpyglassAstronomyClient.stars) {
            if (star.name != null && star.name.equals(name)) {
                return star;
            }
        }
        SpyglassAstronomyClient.say(String.format("No Star with name \"%s\" could be found", name));
        return null;        
    }

    public static String getMessageText(CommandContext<FabricClientCommandSource> context) {
        //a lot of digging through #SayCommand to make a MessageArgumentType that works clientside
        MessageFormat messageFormat = (MessageFormat)context.getArgument("name", MessageFormat.class);
        return messageFormat.getContents();
    }
}