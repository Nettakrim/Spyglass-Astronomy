package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public class AutoCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> autoNode = ClientCommandManager
                .literal("sga:auto")
                .build();

        LiteralCommandNode<FabricClientCommandSource> generateNode = ClientCommandManager
                .literal("generate")
                .executes(AutoCommand::generate)
                .build();

        autoNode.addChild(generateNode);
        return autoNode;
    }

    private static int generate(CommandContext<FabricClientCommandSource> context) {
        Random random = Random.create(SpyglassAstronomyClient.spaceDataManager.getStarSeed());

        int constellations = random.nextBetween(15,20);
        //https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere
        float phi = MathHelper.PI * (MathHelper.sqrt(5f) - 1f);
        for (int i = 0; i < constellations; i++) {
            float x = 1 - (i / (float)(constellations - 1)) * 2;
            float radius = MathHelper.sqrt(1 - x * x);

            float theta = phi * i;

            float y = MathHelper.cos(theta) * radius;
            float z = MathHelper.sin(theta) * radius;

            Vector3f position = new Vector3f(x, y, z);
            position.add((random.nextFloat()*0.3f)-0.15f, (random.nextFloat()*0.3f)-0.15f, (random.nextFloat()*0.3f)-0.15f);
            position.normalize();
            createRandomConstellation(random, position);
        }

        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static void createRandomConstellation(Random random, Vector3f location) {
        Star lastStar = null;
        Vector3f lastPosition = location;
        int lines = 0;
        int maxLines = random.nextBetween(4,8);
        int maxConnections = random.nextBetween(3,5);
        while (lines < maxLines) {
            for (Star star : SpyglassAstronomyClient.stars) {
                if (star.getAlpha() < random.nextFloat()*2) continue;
                Vector3f starPos = star.getPositionAsVector3f();
                if (starPos.distanceSquared(lastPosition) > 0.1f*(star.getAlpha()+0.5f) || starPos.distanceSquared(location) > 0.2f) continue;
                if (lastStar != null) {
                    StarLine starLine = new StarLine(star.index, lastStar.index, true);
                    SpyglassAstronomyClient.addStarLine(starLine, new Constellation(starLine), false, false);
                    lines++;
                    if (lines >= maxLines) break;
                }
                if (star.getConnectedStars() >= maxConnections || random.nextFloat() * star.getAlpha() > 0.25f) {
                    lastStar = star;
                    lastPosition = starPos;
                }
            }
        }
    }
}
