package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.nettakrim.spyglass_astronomy.*;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public class AdminCommand {
    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> adminNode = ClientCommandManager
            .literal("sga:admin")
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

        adminNode.addChild(setStarCountNode);
        adminNode.addChild(bypassNode);
        adminNode.addChild(yearLengthNode);

        adminNode.addChild(getConstellationsNode());
        adminNode.addChild(getSetSeedNode());
        adminNode.addChild(getRenameNode());
        adminNode.addChild(getChangesNode());

        return adminNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getConstellationsNode() {
        LiteralCommandNode<FabricClientCommandSource> constellationsNode = ClientCommandManager
            .literal("constellations")
            .build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .then(
                ClientCommandManager.argument("data", MessageArgumentType.message())
                    .executes(AdminCommand::addConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .executes(AdminCommand::removeSelectedConstellation)
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.constellations)
                    .executes(AdminCommand::removeConstellation)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> generateNode = ClientCommandManager
            .literal("generate")
            .executes(AdminCommand::generateConstellations)
            .build();

        constellationsNode.addChild(addNode);
        constellationsNode.addChild(removeNode);
        constellationsNode.addChild(generateNode);
        return constellationsNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getSetSeedNode() {
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

        setSeedNode.addChild(setStarSeedNode);
        setSeedNode.addChild(setPlanetSeedNode);
        return setSeedNode;
    }


    private static LiteralCommandNode<FabricClientCommandSource> getRenameNode() {
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

        renameNode.addChild(constellationNameNode);
        renameNode.addChild(starNameNode);
        renameNode.addChild(orbitingBodyNameNode);
        return renameNode;
    }

    private static LiteralCommandNode<FabricClientCommandSource> getChangesNode() {
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

        changesNode.addChild(discardNode);
        changesNode.addChild(saveNode);
        changesNode.addChild(queryNode);
        return changesNode;
    }

    private static int setStarCount(CommandContext<FabricClientCommandSource> context) {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        SpyglassAstronomyClient.say("commands.admin.setstarcount", Integer.toString(amount), Integer.toString(SpyglassAstronomyClient.getStarCount()));
        SpyglassAstronomyClient.setStarCount(amount);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int setStarSeed(CommandContext<FabricClientCommandSource> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setseed.star", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getStarSeed()));
        SpyglassAstronomyClient.spaceDataManager.setStarSeed(seed);
        SpyglassAstronomyClient.generateStars(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int setPlanetSeed(CommandContext<FabricClientCommandSource> context) {
        long seed = LongArgumentType.getLong(context, "seed");
        SpyglassAstronomyClient.say("commands.admin.setseed.planet", Long.toString(seed), Long.toString(SpyglassAstronomyClient.spaceDataManager.getPlanetSeed()));
        SpyglassAstronomyClient.spaceDataManager.setPlanetSeed(seed);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
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

    public static int bypassKnowledge(CommandContext<FabricClientCommandSource> context) {
        if (SpyglassAstronomyClient.knowledge.bypassKnowledge()) {
            SpyglassAstronomyClient.say("commands.admin.bypass.on");
        } else {
            SpyglassAstronomyClient.say("commands.admin.bypass.off");
        }
        return 1;
    }

    public static int setYearLength(CommandContext<FabricClientCommandSource> context) {
        float yearLength = FloatArgumentType.getFloat(context, "days");
        SpyglassAstronomyClient.say("commands.admin.setyearlength", Float.toString(yearLength), Float.toString(SpyglassAstronomyClient.spaceDataManager.getYearLength()));
        SpyglassAstronomyClient.spaceDataManager.setYearLength(yearLength);
        SpyglassAstronomyClient.generatePlanets(null, true);
        SpaceDataManager.makeChange();
        return 1;
    }

    private static int addConstellation(CommandContext<FabricClientCommandSource> context) {
        String dataRaw = SpyglassAstronomyCommands.getMessageText(context,"data");
        int index = dataRaw.indexOf(' ');
        Constellation constellation = SpaceDataManager.decodeConstellation(null, dataRaw.substring(index+1), dataRaw.substring(0, index));
        if (constellation.getLines().size() == 0) {
            SpyglassAstronomyClient.say("commands.admin.constellations.add.invalid", constellation.name);
            return -1;
        }

        return addConstellation(constellation, true, true);
    }

    private static int addConstellation(Constellation constellation, boolean select, boolean sayFeedback) {
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
                        if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add.fail", constellation.name, targetConstellation.name);
                        if (select) targetConstellation.select();
                        return -1;
                    }
                } else {
                    if (constellation.isUnnamed() || constellation.name.equals(targetConstellation.name)) {
                        if (sayFeedback) SpyglassAstronomyClient.say("commands.admin.constellations.add.fail", constellation.name, targetConstellation.name);
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
