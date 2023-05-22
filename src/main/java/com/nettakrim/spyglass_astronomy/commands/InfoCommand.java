package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.text.Style;
import org.joml.Vector3f;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.Orbit;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;
import com.nettakrim.spyglass_astronomy.Knowledge.Level;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class InfoCommand implements Command<FabricClientCommandSource> {

    public static LiteralCommandNode<FabricClientCommandSource> getCommandNode() {
        LiteralCommandNode<FabricClientCommandSource> infoNode = ClientCommandManager
            .literal("sga:info")
            .executes(new InfoCommand())
            .build();

        LiteralCommandNode<FabricClientCommandSource> constellationInfoNode = ClientCommandManager
            .literal("constellation")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.constellations)
                    .executes(InfoCommand::getConstellationInfo)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> starInfoNode = ClientCommandManager
            .literal("star")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.stars)
                    .executes(InfoCommand::getStarInfo)
            )
            .build();

        LiteralCommandNode<FabricClientCommandSource> orbitingBodyInfoNode = ClientCommandManager
            .literal("planet")
            .then(
                ClientCommandManager.argument("name", MessageArgumentType.message())
                    .suggests(SpyglassAstronomyCommands.orbitingBodies)
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

        infoNode.addChild(constellationInfoNode);
        infoNode.addChild(starInfoNode);
        infoNode.addChild(orbitingBodyInfoNode);
        infoNode.addChild(earthInfoNode);
        infoNode.addChild(solarSystemInfoNode);
        return infoNode;
    }

	@Override
	public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        if (Constellation.selected != null) {
            displayInfo(Constellation.selected);
            return 1;
        }
        if (Star.selected != null) {
            displayInfo(Star.selected);
            return 1;
        }
        if (OrbitingBody.selected != null) {
            displayInfo(OrbitingBody.selected);
            return 1;
        }
        SpyglassAstronomyClient.say("commands.info.nothingselected");
        return -1;
	}

    private static int getConstellationInfo(CommandContext<FabricClientCommandSource> context) {
        Constellation constellation = SpyglassAstronomyCommands.getConstellation(context);
        if (constellation == null) {
            return -1;
        }
        displayInfo(constellation);
        return 1;
    }

    private static int getStarInfo(CommandContext<FabricClientCommandSource> context) {
        Star star = SpyglassAstronomyCommands.getStar(context);
        if (star == null) {
            return -1;
        }
        displayInfo(star);
        return 1;
    }

    private static int getOrbitingBodyInfo(CommandContext<FabricClientCommandSource> context) {
        OrbitingBody orbitingBody = SpyglassAstronomyCommands.getOrbitingBody(context);
        if (orbitingBody == null) {
            return -1;
        }
        displayInfo(orbitingBody);
        return 1;
    }

    private static int getEarthInfo(CommandContext<FabricClientCommandSource> context) {
        displayEarthInfo();
        return 1;
    }

    private static int getSolarSystemInfo(CommandContext<FabricClientCommandSource> context) {
        displaySolarSystemInfo();
        return 1;
    }

    private static void displayInfo(Constellation constellation) {
        int[] flags = new int[] {-1, -1};
        MutableText text = Text.empty();
        text.append(translate("constellation.name", constellation.name));

        Vector3f position = constellation.getAveragePosition();
        staticVisibilityInfo(text, position, flags);

        text.append(SpyglassAstronomyClient.knowledge.getKnowledgeInstructions(flags));
        text.setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor));
        SpyglassAstronomyClient.longSay(text);
    }

    private static void displayInfo(Star star) {
        int[] flags = new int[] {-1, -1};
        MutableText text = Text.empty();
        text.append(translate("star.name", star.isUnnamed() ? "Unnamed" : star.name));

        Vector3f position = star.getPositionAsVector3f();
        staticVisibilityInfo(text, position, flags);

        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.MASTER, flags)) {
            //most visible stars are within 1000 light years, the stars index is used to add a bit of randomness to the distance
            float alpha = star.getAlpha();
            int distance = (int)((1-alpha)*1000);
            distance += star.index%100;
            if (distance < 1) distance = 1;
            text.append(translate("star.distance", Integer.toString(distance)));
        }

        text.append(SpyglassAstronomyClient.knowledge.getKnowledgeInstructions(flags));
        text.setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor));
        SpyglassAstronomyClient.longSay(text);
    }

    private static void displayInfo(OrbitingBody orbitingBody) {
        int[] flags = new int[] {-1, -1};
        MutableText text = Text.empty();
        text.append(translate("planet.name", orbitingBody.isUnnamed() ? "Unnamed" : orbitingBody.name));
        text.append(translate("planet.type."+orbitingBody.type.toString().toLowerCase()));
        orbitInfo(text, orbitingBody.orbit, flags);

        text.append(SpyglassAstronomyClient.knowledge.getKnowledgeInstructions(flags));
        text.setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor));
        SpyglassAstronomyClient.longSay(text);
    }

    private static void displayEarthInfo() {
        int[] flags = new int[] {-1, -1};
        MutableText text = Text.empty();
        text.append(translate("thisworld.time", getMinecraftTime()));
        text.append(translate("thisworld.moonphase")).append(translate("moonphase."+Integer.toString(SpyglassAstronomyClient.world.getMoonPhase(), SINGLE_SUCCESS)));
        orbitInfo(text, SpyglassAstronomyClient.earthOrbit, flags);

        text.append(SpyglassAstronomyClient.knowledge.getKnowledgeInstructions(flags));
        text.setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor));
        SpyglassAstronomyClient.longSay(text);
    }

    private static void displaySolarSystemInfo() {
        int[] flags = new int[] {-1, -1};
        MutableText text = Text.empty();
        text.append(translate("solarsystem.planets"));
        int stage = 0;
        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            if (stage == 0 && orbitingBody.orbit.period > SpyglassAstronomyClient.earthOrbit.period) {
                text.append(translate("solarsystem.thisworld"));
                stage = 1;
            }
            if (stage == 1 && !orbitingBody.isPlanet) {
                if (!SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.ADEPT, flags)) break;
                text.append(translate("solarsystem.comets"));
                stage = 2;
            }
            if (orbitingBody.isUnnamed()) {
                text.append(translate("solarsystem.unknown"));
            } else {
                text.append(translate("solarsystem.named", orbitingBody.name));
            }
        }

        if (SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.EXPERT, flags)) {
            text.append(translate("solarsystem.time", Long.toString(SpyglassAstronomyClient.getDay()), Float.toString(MathHelper.floor(SpyglassAstronomyClient.getDayFraction()*100)/100f).replace("0.","")));
        }

        text.append(SpyglassAstronomyClient.knowledge.getKnowledgeInstructions(flags));
        text.setStyle(Style.EMPTY.withColor(SpyglassAstronomyClient.textColor));
        SpyglassAstronomyClient.longSay(text);
    }

    private static void staticVisibilityInfo(MutableText text, Vector3f position, int[] flags) {
        Vector3f pos = new Vector3f(position);
        pos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(45f));
        pos.rotate(RotationAxis.POSITIVE_X.rotationDegrees(SpyglassAstronomyClient.getStarAngle()));
        pos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));

        float yaw = (float)(Math.atan2(pos.x, pos.z)*-180d/Math.PI);
        float angle = (float)(Math.atan2(Math.sqrt(pos.x * pos.x + pos.z * pos.z), pos.y)*180d/Math.PI)-90;
        
        text.append(translate("visibility.angle", prettyFloat(yaw), prettyFloat(angle)));

        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT, flags)) {
            pos = new Vector3f(position);
            pos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(45f));
            pos.rotate(RotationAxis.POSITIVE_X.rotationDegrees(SpyglassAstronomyClient.starAngleMultiplier*(0.75f/SpyglassAstronomyClient.earthOrbit.period)));
            pos.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
            if (MathHelper.abs(pos.z) < 0.9f) {
                float referenceYaw = (float)(Math.atan2(pos.x, pos.z)*-180d/Math.PI);
                angle = (float)(Math.atan2(Math.sqrt(pos.x * pos.x + pos.z * pos.z), pos.y)*180d/Math.PI)-90;
                if (referenceYaw < 0) angle = 180 - angle;
                if (angle < 0) angle += 360;
                float period = SpyglassAstronomyClient.earthOrbit.period;
                angle = (period - MathHelper.floor((angle/360)*period+0.5f)) % period;
                int nearestDay = (int)angle;
                if (period == 8) {
                    text.append(translate("visibility.time.moonphase")).append(translate("moonphase."+ nearestDay));
                } else {
                    int inDays = nearestDay - ((int)(SpyglassAstronomyClient.getDay()%((long)period)));
                    if (inDays < 0) inDays += 8;
                    text.append(translate("visibility.time.date", nearestDay, inDays));
                }
            } else {
                text.append(translate("visibility.time.always"));
            }
        }
    }

    private static void orbitInfo(MutableText text, Orbit orbit, int[] flags) {
        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.NOVICE, flags)) {
            text.append(translate("orbit.period", prettyFloat(orbit.period)));
        }

        boolean isEarth = orbit == SpyglassAstronomyClient.earthOrbit;

        if (!isEarth) {
            if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT, flags)) {
                float max = Math.max(orbit.period, SpyglassAstronomyClient.earthOrbit.period);
                float min = Math.min(orbit.period, SpyglassAstronomyClient.earthOrbit.period);
                // max/min is often 1 less iteration for getDenominator
                double[] fraction = getFraction(max/min);

                float resonance = ((float)fraction[1]*max)/(float)(fraction[0] - fraction[1]);
                text.append(translate("orbit.resonance", prettyFloat(resonance)));
            }
        }

        if ((isEarth && SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT, flags)) || SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.EXPERT, flags)) {
            text.append(translate("orbit.position",Integer.toString((int)(orbit.lastLocalTime*100))));
        }
        if (!isEarth) {
            if (SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.EXPERT, flags)) {
                Vector3f pos = orbit.getLastRotatedPosition();

                Vector3f earthPos = SpyglassAstronomyClient.earthOrbit.getLastRotatedPosition();
                pos.sub(earthPos);
                float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(pos.x, pos.y, pos.z);

                text.append(translate("orbit.distance", prettyFloat(MathHelper.sqrt(sqrDistance)/SpyglassAstronomyClient.earthOrbit.semiMajorAxis)));

                pos.normalize();
                pos.rotate(RotationAxis.POSITIVE_Z.rotationDegrees((SpyglassAstronomyClient.getPositionInOrbit(360f)*(1-1/SpyglassAstronomyClient.earthOrbit.period)+180)));

                float yaw = (float)(Math.atan2(pos.x, pos.z)*-180d/Math.PI);
                float angle = (float)(Math.atan2(Math.sqrt(pos.x * pos.x + pos.z * pos.z), pos.y)*180d/Math.PI)-90;

                text.append(translate("orbit.angle", prettyFloat(yaw), prettyFloat(angle)));
            }
        }

        if (SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.MASTER, flags)) {
            text.append(translate("orbit.eccentricity", prettyFloat(orbit.eccentricity)));
            text.append(translate("orbit.ascension", prettyFloat(orbit.ascension)));
            text.append(translate("orbit.inclination", prettyFloat(orbit.inclination)));
        }
    }

    static private double[] getFraction(double x){
        double tolerance = 1.0E-6;
        double h1=1; double h2=0;
        double k1=0; double k2=1;
        double b = x;
        do {
            double a = Math.floor(b);
            double aux = h1; h1 = a*h1+h2; h2 = aux;
            aux = k1; k1 = a*k1+k2; k2 = aux;
            b = 1/(b-a);
        } while (Math.abs(x-h1/k1) > x*tolerance);

        return new double[] {h1,k1};
    }

    private static String prettyFloat(float f) {
        if (f == MathHelper.floor(f)) {
            return Integer.toString((int)f);
        } else {
            f = Math.round(f*100);
            return Float.toString(f/100);
        }        
    }

    //https://github.com/Iru21/TimeDisplay/blob/master/src/main/kotlin/me/iru/timedisplay/TimeUtils.kt
    private static String getMinecraftTime() {
        long timeDay = SpyglassAstronomyClient.world.getTimeOfDay();
        int dayTicks = (int)(timeDay % 24000);
        int hour = (dayTicks / 1000 + 6) % 24;
        int min = ((int)(dayTicks / 16.666666f)) % 60;
        int sec = ((int)(dayTicks / 0.277777f)) % 60;
        return formatTime(hour, min, sec);
    }

    private static String formatTime(int hour, int min, int sec) {
        String time = Integer.toString(sec);
        if (time.length() == 1) time = "0"+time;
        time = min + ":" + time;
        if (time.length() == 4) time ="0"+time;
        time = hour + ":" + time;
        if (time.length() == 7) time ="0"+time;
        return time;   
    }

    private static Text translate(String key, Object... formatting) {
        return Text.translatable(SpyglassAstronomyClient.MODID+".commands.info."+key, formatting);
    }
}
