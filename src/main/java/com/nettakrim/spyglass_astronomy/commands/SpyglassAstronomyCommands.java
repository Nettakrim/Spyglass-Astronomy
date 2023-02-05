package com.nettakrim.spyglass_astronomy.commands;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.MessageArgumentType.MessageFormat;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SpyglassAstronomyCommands {
    public static SuggestionProvider<FabricClientCommandSource> constellations = (context, builder) -> {
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            builder.suggest(constellation.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static SuggestionProvider<FabricClientCommandSource> stars = (context, builder) -> {
        for (Star star : SpyglassAstronomyClient.stars) {
            if (star.name != null) builder.suggest(star.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static SuggestionProvider<FabricClientCommandSource> orbitingBodies = (context, builder) -> {
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (orbitingBody.name != null) builder.suggest(orbitingBody.name);
        }
        return CompletableFuture.completedFuture(builder.build());
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            RootCommandNode<FabricClientCommandSource> root = dispatcher.getRoot();

            registerInfoNode(root);

            registerSelectNode(root);

            registerNameNode(root);

            registerShareNode(root);

            registerAdminNode(root);

            registerHideNode(root);
        });
    }

    public static void registerInfoNode(RootCommandNode<FabricClientCommandSource> root) {
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

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyInfoNode = ClientCommandManager
        .literal("planet")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(orbitingBodies)
            .executes(InfoCommand::getOrbitingBodyInfo)
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> earthInfoNode = ClientCommandManager
        .literal("thisworld")
        .executes(InfoCommand::getEarthInfo)
        .build();
        
        LiteralCommandNode<FabricClientCommandSource> solarSystemInfoNode = ClientCommandManager
        .literal("solarsystem")
        .executes(InfoCommand::getSolarSystemInfo)
        .build();        
    
        root.addChild(infoNode);
        infoNode.addChild(constellationInfoNode);
        infoNode.addChild(starInfoNode);
        infoNode.addChild(orbitingBodyInfoNode);
        infoNode.addChild(earthInfoNode);
        infoNode.addChild(solarSystemInfoNode);
    }

    public static void registerSelectNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> selectNode = ClientCommandManager
        .literal("sga:select")
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

        LiteralCommandNode<FabricClientCommandSource> orbitingBodySelectNode = ClientCommandManager
        .literal("planet")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(orbitingBodies)
            .executes(SelectCommand::selectOrbitingBody)
        )
        .build();

        root.addChild(selectNode);
        selectNode.addChild(constellationSelectNode);
        selectNode.addChild(starSelectNode);
        selectNode.addChild(orbitingBodySelectNode);
    }

    public static void registerNameNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
        .literal("sga:name")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .executes(new NameCommand())
        )
        .build();

        root.addChild(nameNode);
    }

    public static void registerShareNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> shareNode = ClientCommandManager
        .literal("sga:share")
        .executes(new ShareCommand())
        .build();

        LiteralCommandNode<FabricClientCommandSource> constellationShareNode = ClientCommandManager
        .literal("constellation")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(constellations)
            .executes(ShareCommand::shareConstellation)
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> starShareNode = ClientCommandManager
        .literal("star")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(stars)
            .executes(ShareCommand::shareStar)
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyShareNode = ClientCommandManager
        .literal("planet")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(orbitingBodies)
            .executes(ShareCommand::shareOrbitingBody)
        )
        .build();

        root.addChild(shareNode);
        shareNode.addChild(constellationShareNode);
        shareNode.addChild(starShareNode);
        shareNode.addChild(orbitingBodyShareNode);
    }

    public static void registerAdminNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> adminNode = ClientCommandManager
        .literal("sga:admin")
        .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
        .literal("removeconstellation")
        .then(
            ClientCommandManager.argument("name", MessageArgumentType.message())
            .suggests(constellations)
            .executes(AdminCommand::removeConstellation)
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> setStarCountNode = ClientCommandManager
        .literal("setstarcount")
        .then(
            ClientCommandManager.argument("amount", IntegerArgumentType.integer(0,4095))
            .executes(AdminCommand::setStarCount)
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> bypassNode = ClientCommandManager
        .literal("bypassknowledge")
        .executes(AdminCommand::bypassKnowledge)
        .build();

        LiteralCommandNode<FabricClientCommandSource> yearLengthNode = ClientCommandManager
        .literal("setyearlength")
        .then(
            ClientCommandManager.argument("days", FloatArgumentType.floatArg(1f/8f))
            .executes(AdminCommand::setYearLength)
        )
        .build();

        root.addChild(adminNode);
        adminNode.addChild(removeNode);
        adminNode.addChild(setStarCountNode);
        adminNode.addChild(bypassNode);
        adminNode.addChild(yearLengthNode);

        registerAdminAddNode(adminNode);
        registerAdminSetSeedNode(adminNode);
        registerAdminRenameNode(adminNode);
        registerAdminChangesNode(adminNode);
    }

    public static void registerAdminAddNode(LiteralCommandNode<FabricClientCommandSource> node) {
        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
        .literal("add")
        .build();

        LiteralCommandNode<FabricClientCommandSource> addConstellationNode = ClientCommandManager
        .literal("constellation")
        .then(
            ClientCommandManager.argument("data", MessageArgumentType.message())
            .executes(AdminCommand::addConstellation)
        )
        .build();

        node.addChild(addNode);
        addNode.addChild(addConstellationNode);
    }

    public static void registerAdminSetSeedNode(LiteralCommandNode<FabricClientCommandSource> node) {
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

        LiteralCommandNode<FabricClientCommandSource> setPlanetSeedNode = ClientCommandManager
        .literal("planet")
        .then(
            ClientCommandManager.argument("seed", LongArgumentType.longArg())
            .executes(AdminCommand::setPlanetSeed)
        )
        .build();

        node.addChild(setSeedNode);
        setSeedNode.addChild(setStarSeedNode);
        setSeedNode.addChild(setPlanetSeedNode);
    }


    public static void registerAdminRenameNode(LiteralCommandNode<FabricClientCommandSource> node) {
        LiteralCommandNode<FabricClientCommandSource> renameNode = ClientCommandManager
        .literal("rename")
        .build();

        LiteralCommandNode<FabricClientCommandSource> constellationNameNode = ClientCommandManager
        .literal("constellation")
        .then(
            ClientCommandManager.argument("index", IntegerArgumentType.integer())
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                .executes(NameCommand::nameConstellation)
            )
        )
        .build();

        LiteralCommandNode<FabricClientCommandSource> starNameNode = ClientCommandManager
        .literal("star")
        .then(
            ClientCommandManager.argument("index", IntegerArgumentType.integer())
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                .executes(NameCommand::nameStar)
            )
        )
        .build();        

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyNameNode = ClientCommandManager
        .literal("planet")
        .then(
            ClientCommandManager.argument("index", IntegerArgumentType.integer())
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                .executes(NameCommand::nameOrbitingBody)
            )
        )
        .build();

        node.addChild(renameNode);
        renameNode.addChild(constellationNameNode);
        renameNode.addChild(starNameNode);
        renameNode.addChild(orbitingBodyNameNode);
    }

    public static void registerAdminChangesNode(LiteralCommandNode<FabricClientCommandSource> node) {
        LiteralCommandNode<FabricClientCommandSource> changesNode = ClientCommandManager
        .literal("changes")
        .build();

        LiteralCommandNode<FabricClientCommandSource> discardNode = ClientCommandManager
        .literal("discard")
        .executes(AdminCommand::discardUnsavedChanges)
        .build();

        LiteralCommandNode<FabricClientCommandSource> saveNode = ClientCommandManager
        .literal("save")
        .executes(AdminCommand::saveChanges)
        .build();
        
        LiteralCommandNode<FabricClientCommandSource> queryNode = ClientCommandManager
        .literal("query")
        .executes(AdminCommand::queryChanges)
        .build();

        node.addChild(changesNode);
        changesNode.addChild(discardNode);
        changesNode.addChild(saveNode);
        changesNode.addChild(queryNode);
    }

    public static void registerHideNode(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> hideNode = ClientCommandManager
        .literal("sga:hide")
        .executes(new HideCommand())
        .build();

        LiteralCommandNode<FabricClientCommandSource> constellationsHideNode = ClientCommandManager
        .literal("constellations")
        .executes(HideCommand::hideConstellations)
        .build();

        LiteralCommandNode<FabricClientCommandSource> starsHideNode = ClientCommandManager
        .literal("stars")
        .executes(HideCommand::hideStars)
        .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodiesHideNode = ClientCommandManager
        .literal("planets")
        .executes(HideCommand::hideOrbitingBodies)
        .build();

        LiteralCommandNode<FabricClientCommandSource> oldStarsHideNode = ClientCommandManager
        .literal("vanillastars")
        .executes(HideCommand::hideOldStars)
        .build();


        root.addChild(hideNode);
        hideNode.addChild(constellationsHideNode);
        hideNode.addChild(starsHideNode);
        hideNode.addChild(orbitingBodiesHideNode);
        hideNode.addChild(oldStarsHideNode);
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
        MessageFormat messageFormat = (MessageFormat)context.getArgument(name, MessageFormat.class);
        return messageFormat.getContents();
    }

    public static Text getClickHere(String actionKey, String command, boolean run, Object... formatting) {
        return Text.translatable(SpyglassAstronomyClient.MODID+".commands.share.click").setStyle(Style.EMPTY
        .withClickEvent(
            new ClickEvent(run ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command)
        )
        .withColor(Formatting.GREEN))
        .append(Text.translatable(SpyglassAstronomyClient.MODID+"."+actionKey, formatting).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
    }
}