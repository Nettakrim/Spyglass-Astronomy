package com.nettakrim.spyglass_astronomy;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.MessageArgumentType.MessageFormat;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class SpyglassAstronomyCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("nameconstellation")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .executes(SpyglassAstronomyCommands::NameConstellation)
                )
            );

            SuggestionProvider<FabricClientCommandSource> constellations = (context, builder) -> {
                for (Constellation constellation : SpyglassAstronomyClient.constellations) {
                    builder.suggest(constellation.name);
                }
                return CompletableFuture.completedFuture(builder.build());
            };

            dispatcher.register(
                ClientCommandManager.literal("constellationinfo")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(constellations)
                    .executes(SpyglassAstronomyCommands::GetConstellationInfo)
                )
            );

            dispatcher.register(
                ClientCommandManager.literal("constellationinfo")
                .executes(SpyglassAstronomyCommands::GetActiveConstellationInfo)
            );

            dispatcher.register(
                ClientCommandManager.literal("selectconstellation")
                .then(
                    ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(constellations)
                    .executes(SpyglassAstronomyCommands::SelectConstellation)
                )
            );            
        });
    }

    public static int NameConstellation(CommandContext<FabricClientCommandSource> context) {
        //a lot of digging through #SayCommand to make a MessageArgumentType that works clientside
        MessageFormat messageFormat = (MessageFormat)context.getArgument("name", MessageFormat.class);
        return SpyglassAstronomyClient.nameActiveConstellation(messageFormat.getContents()) ? 1 : -1;
    }

    public static int GetActiveConstellationInfo(CommandContext<FabricClientCommandSource> context) {
        if (SpyglassAstronomyClient.activeConstellation != null && SpyglassAstronomyClient.activeConstellation.isActive) {
            GetInfo(SpyglassAstronomyClient.activeConstellation);
            return 1;
        }
        SpyglassAstronomyClient.say(String.format("No Constellation currently selected"));
        return -1;
    }

    public static int GetConstellationInfo(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        GetInfo(constellation);
        return 1;
    }

    public static int SelectConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        if (!SpyglassAstronomyClient.client.player.getActiveItem().isOf(Items.SPYGLASS)) {
            SpyglassAstronomyClient.say("Spyglass must be held to be able to select constellations");
            return -1;
        }
        SpyglassAstronomyClient.setActiveConstellation(constellation.getLines().get(0).getStars()[0], true);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        return 1;
    }

    private static Constellation getConstellation(CommandContext<FabricClientCommandSource> context) {
        MessageFormat messageFormat = (MessageFormat)context.getArgument("name", MessageFormat.class);
        String name = messageFormat.getContents();
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            if (constellation.name.equals(name)) {
                return constellation;
            }
        }
        SpyglassAstronomyClient.say(String.format("No Constellation with name \"%s\" could be found", name));
        return null;        
    }

    private static void GetInfo(Constellation constellation) {
        Vec3f averagePosition = constellation.getAveragePosition();
        averagePosition.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        averagePosition.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(1.75f * 405f));
        averagePosition.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45f));
        String mostVisisbleDuring;
        if (MathHelper.abs(averagePosition.getX()) < 0.9f) {
            float angle = (float)(MathHelper.atan2(averagePosition.getY(), averagePosition.getZ())*180d/Math.PI);
            int mostVisiblePhase = Math.round(angle/45)-1;
            if (mostVisiblePhase < 0) mostVisiblePhase += 8;
            mostVisisbleDuring = SpyglassAstronomyClient.getMoonPhaseName(mostVisiblePhase);
        } else {
            mostVisisbleDuring = "Always";
        }

        SpyglassAstronomyClient.longSay("Name: "+constellation.name+"\nMost Visible During: "+mostVisisbleDuring);
    }
}