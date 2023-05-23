package com.nettakrim.spyglass_astronomy.commands.admin_subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.*;
import com.nettakrim.spyglass_astronomy.commands.SpyglassAstronomyCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public class ConstellationsCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> constellationsNode = ClientCommandManager
            .literal("constellations")
            .build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .then(
                ClientCommandManager.argument("data", MessageArgumentType.message())
                    .executes(ConstellationsCommand::addConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .executes(ConstellationsCommand::removeSelectedConstellation)
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.constellations)
                    .executes(ConstellationsCommand::removeConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeAllNode = ClientCommandManager
            .literal("removeall")
            .executes(ConstellationsCommand::removeAllConstellations)
            .build();

        LiteralCommandNode<FabricClientCommandSource> generateNode = ClientCommandManager
            .literal("generate")
            .executes(ConstellationsCommand::generateConstellations)
            .build();

        constellationsNode.addChild(addNode);
        constellationsNode.addChild(removeNode);
        constellationsNode.addChild(removeAllNode);
        constellationsNode.addChild(generateNode);
        return constellationsNode;
    }

    private static int addConstellation(CommandContext<FabricClientCommandSource> context) {
        String dataRaw = SpyglassAstronomyCommands.getMessageText(context,"data");
        int index = dataRaw.indexOf(' ');
        if (index == -1) {
            SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.data", dataRaw);
            return -1;
        }
        Constellation constellation = SpaceDataManager.decodeConstellation(null, dataRaw.substring(index+1), dataRaw.substring(0, index));
        if (constellation.getLines().size() == 0) {
            SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.data", constellation.name);
            return -1;
        }

        int max = 0;
        for (StarLine line : constellation.getLines()) {
            int star = line.getOtherStar(-1);
            max = Math.max(Math.max(line.getOtherStar(star), star), max);
        }
        if (max >= SpyglassAstronomyClient.getStarCount()) {
            if (max < 4096) {
                SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.stars", constellation.name, max, SpyglassAstronomyClient.getStarCount());
            } else {
                SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.data", constellation.name);
            }
            return -1;
        }

        return addConstellation(constellation, true, true);
    }

    public static int addConstellation(Constellation constellation, boolean select, boolean sayFeedback) {
        Constellation potentialMatch = null;
        for (Constellation targetConstellation : SpyglassAstronomyClient.constellations) {
            boolean intersects = false;
            boolean isDifferent = false;
            for (StarLine line : constellation.getLines()) {
                if (targetConstellation.lineIntersects(line)) {
                    intersects = true;
                } else {
                    isDifferent = true;
                }
                if (intersects && targetConstellation.hasNoMatchingLine(line)) {
                    isDifferent = true;
                    break;
                }
            }

            if (intersects) {
                if (!isDifferent) {
                    for (StarLine line : targetConstellation.getLines()) {
                        if (constellation.hasNoMatchingLine(line)) {
                            isDifferent = true;
                            break;
                        }
                    }
                }

                if (isDifferent) {
                    if (potentialMatch == null && !constellation.isUnnamed() && constellation.name.equals(targetConstellation.name)) {
                        potentialMatch = targetConstellation;
                    } else {
                        if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.collision", constellation.name, targetConstellation.name);
                        if (select) targetConstellation.select();
                        return -1;
                    }
                } else {
                    if (constellation.isUnnamed() || constellation.name.equals(targetConstellation.name)) {
                        if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add.fail.collision", constellation.name, targetConstellation.name);
                        if (select) targetConstellation.select();
                        return -1;
                    } else {
                        if (sayFeedback) SpyglassAstronomyClient.say("commands.name.constellation.rename", targetConstellation.name, constellation.name);
                        targetConstellation.name = constellation.name;
                        if (select) targetConstellation.select();
                        SpaceDataManager.makeChange();
                        return 1;
                    }
                }
            }
        }

        if (potentialMatch == null) {
            if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add", constellation.name);
        } else {
            if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add.edit", potentialMatch.name);
            clearConnections(potentialMatch);
            SpyglassAstronomyClient.constellations.remove(potentialMatch);
        }

        if (select) constellation.select();
        constellation.initaliseStarLines();
        SpyglassAstronomyClient.constellations.add(constellation);

        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int removeConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        return removeConstellation(constellation);
    }

    private static int removeSelectedConstellation(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = Constellation.selected;
        Constellation.deselect();
        return removeConstellation(constellation);
    }

    private static int removeConstellation(Constellation constellation) {
        if (constellation == null) {
            SpyglassAstronomyClient.say("commands.admin.constellations.remove.nothingselected");
            return -1;
        }
        SpyglassAstronomyClient.say("commands.admin.constellations.remove", constellation.name);
        clearConnections(constellation);
        SpyglassAstronomyClient.constellations.remove(constellation);
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int removeAllConstellations(CommandContext<FabricClientCommandSource> context) {
        SpyglassAstronomyClient.say("commands.admin.constellations.remove.all", SpyglassAstronomyClient.constellations.size());
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            clearConnections(constellation);
        }
        SpyglassAstronomyClient.constellations.clear();
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int generateConstellations(CommandContext<FabricClientCommandSource> context) {
        Random random = Random.create(SpyglassAstronomyClient.spaceDataManager.getStarSeed());

        int constellations = random.nextBetween(15,20);
        int spawned = 0;
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

            if (createRandomConstellation(random, position)) {
                spawned++;
            }
        }

        SpyglassAstronomyClient.say("commands.admin.constellations.generate", Integer.toString(spawned));
        SpyglassAstronomyClient.spaceRenderingManager.scheduleConstellationsUpdate();
        SpaceDataManager.makeChange();
        return 1;
    }

    private static boolean createRandomConstellation(Random random, Vector3f location) {
        Star lastStar = null;
        Vector3f lastPosition = location;
        int lines = 0;
        int maxLines = random.nextBetween(4,8);
        int maxConnections = random.nextBetween(3,5);
        float maxAngle = maxLines <= 5 ? 0.98f : (maxConnections <= 3 ? 0.975f : 0.97f);
        Constellation constellation = new Constellation();
        while (lines < maxLines) {
            for (Star star : SpyglassAstronomyClient.stars) {
                if (star.getAlpha() < random.nextFloat()*2) continue;
                Vector3f starPos = star.getPositionAsVector3f();

                if (starPos.distanceSquared(lastPosition) > 0.1f*(star.getAlpha()+0.5f) || starPos.distanceSquared(location) > 0.2f) continue;
                if (lastStar != null) {
                    StarLine starLine = new StarLine(star.index, lastStar.index, false);
                    Vector3f direction = new Vector3f(starPos).sub(lastPosition);

                    boolean failedCheck = false;
                    for (StarLine line : constellation.getLines()) {
                        if (line.intersects(star.index, lastStar.index)) {
                            Star[] stars = line.getStars();
                            if (MathHelper.abs(direction.angleCos(stars[0].getPositionAsVector3f().sub(stars[1].getPositionAsVector3f()))) > maxAngle) {
                                failedCheck = true;
                                break;
                            }
                        }
                    }
                    if (failedCheck) continue;
                    constellation.addLine(starLine);
                    lines++;
                    if (lines >= maxLines) break;
                }
                int connections = 0;
                for (StarLine line : constellation.getLines()) {
                    if (line.hasStar(star.index)) connections++;
                }
                if (connections >= maxConnections || random.nextFloat() * star.getAlpha() > 0.25f) {
                    lastStar = star;
                    lastPosition = starPos;
                }
            }
        }

        return addConstellation(constellation, false, false) == 1;
    }

    private static void clearConnections(Constellation constellation) {
        for (StarLine line : constellation.getLines()) {
            for (Star star : line.getStars()) {
                star.clearAllConnections();
            }
        }
    }
}
