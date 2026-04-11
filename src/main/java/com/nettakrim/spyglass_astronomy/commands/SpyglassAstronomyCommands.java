package com.nettakrim.spyglass_astronomy.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.MessageArgument.Message;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

public class SpyglassAstronomyCommands {
    public static final SuggestionProvider<FabricClientCommandSource> constellations = (context, builder) -> {
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            builder.suggest(constellation.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> stars = (context, builder) -> {
        for (Star star : SpyglassAstronomyClient.stars) {
            if (!star.isUnnamed()) builder.suggest(star.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static final SuggestionProvider<FabricClientCommandSource> orbitingBodies = (context, builder) -> {
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (!orbitingBody.isUnnamed()) builder.suggest(orbitingBody.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            root.addChild(AdminCommand.getCommandNode());
            root.addChild(HideCommand.getCommandNode());
            root.addChild(InfoCommand.getCommandNode());
            root.addChild(NameCommand.getCommandNode());
            root.addChild(SelectCommand.getCommandNode());
            root.addChild(ShareCommand.getCommandNode());
        });
    }

    public static Constellation getConstellation(CommandContext<FabricClientCommandSource> context) {
        String name = getMessageText(context);
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            if (constellation.name.equals(name)) {
                return constellation;
            }
        }
        SpyglassAstronomyClient.say("commands.find.constellation.fail", name);
        return null;
    }

    public static Star getStar(CommandContext<FabricClientCommandSource> context) {
        String name = getMessageText(context);
        for (Star star : SpyglassAstronomyClient.stars) {
            if (star.name != null && star.name.equals(name)) {
                return star;
            }
        }
        SpyglassAstronomyClient.say("commands.find.star.fail", name);
        return null;        
    }

    public static OrbitingBody getOrbitingBody(CommandContext<FabricClientCommandSource> context) {
        String name = getMessageText(context);
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (orbitingBody.name != null && orbitingBody.name.equals(name)) {
                return orbitingBody;
            }
        }
        SpyglassAstronomyClient.say("commands.find.planet.fail", name);
        return null;        
    }

    public static String getMessageText(CommandContext<FabricClientCommandSource> context) {
        return getMessageText(context, "name");
    }

    public static String getMessageText(CommandContext<FabricClientCommandSource> context, String name) {
        //a lot of digging through #SayCommand to make a MessageArgumentType that works clientside
        Message messageFormat = context.getArgument(name, Message.class);
        return messageFormat.text();
    }

    public static Component getClickHere(String actionKey, String command, boolean run, Object... formatting) {
        return Component.translatable(SpyglassAstronomyClient.MODID+".commands.share.click")
                .setStyle(Style.EMPTY.withClickEvent(run ? new ClickEvent.RunCommand(command) : new ClickEvent.SuggestCommand(command)).withColor(SpyglassAstronomyClient.buttonTextColor))
                .append(Component.translatable(SpyglassAstronomyClient.MODID+"."+actionKey, formatting).setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor)));
    }
}