package com.nettakrim.spyglass_astronomy.commands;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public class InfoCommand implements Command<FabricClientCommandSource> {
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
        return -1;
	}

    public static int getConstellationInfo(CommandContext<FabricClientCommandSource> context) {
        displayInfo(SpyglassAstronomyCommands.getConstellation(context));
        return 1;
    }

    public static int getStarInfo(CommandContext<FabricClientCommandSource> context) {
        displayInfo(SpyglassAstronomyCommands.getStar(context));
        return 1;
    }

    public static int getOrbitingBodyInfo(CommandContext<FabricClientCommandSource> context) {
        displayInfo(SpyglassAstronomyCommands.getOrbitingBody(context));
        return 1;
    }

    public static int getEarthInfo(CommandContext<FabricClientCommandSource> context) {
        displayEarthInfo();
        return 1;
    }

    private static void displayInfo(Constellation constellation) {
        boolean[] flags = new boolean[2];
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(constellation.name);
        builder.append('\n');

        Vec3f position = constellation.getAveragePosition();
        staticVisibilityInfo(builder, position, flags);

        if (flags[0]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextStarKnowledgeStage());
        if (flags[1]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextOrbitKnowledgeStage());

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayInfo(Star star) {
        boolean[] flags = new boolean[2];
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(star.name == null ? "Unnamed" : star.name);
        builder.append('\n');

        Vec3f position = star.getPositionAsVec3f();
        staticVisibilityInfo(builder, position, flags);

        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.MASTER)) {
            //most visible stars are within 1000 light years, the stars index is used to add a bit of randomness to the distance
            float alpha = star.getAlpha();
            int distance = (int)((1-alpha)*1000);
            distance += star.index%100;
            if (distance < 1) distance = 1;
            builder.append("\nDistance: ");
            builder.append(distance);
            builder.append(" LY");
        } else {
            flags[0] = true;
        }

        if (flags[0]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextStarKnowledgeStage());
        if (flags[1]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextOrbitKnowledgeStage());

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayInfo(OrbitingBody orbitingBody) {
        boolean[] flags = new boolean[2];
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(orbitingBody.name == null ? "Unnamed" : orbitingBody.name);
        builder.append('\n');

        orbitInfo(builder, orbitingBody.orbit, flags);

        if (flags[0]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextStarKnowledgeStage());
        if (flags[1]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextOrbitKnowledgeStage());

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayEarthInfo() {
        boolean[] flags = new boolean[2];
        StringBuilder builder = new StringBuilder();
        builder.append("Time: ");
        getMinecraftTime(builder);
        builder.append('\n');

        orbitInfo(builder, SpyglassAstronomyClient.earthOrbit, flags);

        if (flags[0]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextStarKnowledgeStage());
        if (flags[1]) builder.append(SpyglassAstronomyClient.knowledge.getInstructionsToNextOrbitKnowledgeStage());


        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void staticVisibilityInfo(StringBuilder builder, Vec3f position, boolean[] flags) {
        Vec3f pos = position.copy();
        pos.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(45f));
        pos.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getStarAngle()));
        pos.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
        SpyglassAstronomyClient.LOGGER.info(pos.toString());

        float yaw = (float)(Math.atan2(pos.getX(), pos.getZ())*-180d/Math.PI);
        float angle = (float)(Math.atan2(Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ()), pos.getY())*180d/Math.PI)-90;
        builder.append("Current Angle: ");
        prettyFloat(builder, yaw);
        builder.append(" ");
        prettyFloat(builder, angle);

        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT)) {
            pos = position.copy();
            pos.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(45f));
            pos.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.starAngleMultiplier*(0.75f/SpyglassAstronomyClient.earthOrbit.period)));
            pos.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
            builder.append("\nMost visible during: ");
            if (MathHelper.abs(pos.getZ()) < 0.9f) {
                float referenceYaw = (float)(Math.atan2(pos.getX(), pos.getZ())*-180d/Math.PI);
                angle = (float)(Math.atan2(Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ()), pos.getY())*180d/Math.PI)-90;
                if (referenceYaw < 0) angle = 180 - angle;
                if (angle < 0) angle += 360;
                float period = SpyglassAstronomyClient.earthOrbit.period;
                angle = (period - MathHelper.floor((angle/360)*period+0.5f)) % period;
                int nearestDay = (int)angle;
                if (period == 8) {
                    builder.append(SpyglassAstronomyClient.getMoonPhaseName(nearestDay));
                } else {
                    builder.append(nearestDay);
                    builder.append(" Days into a year");
                }
                int inDays = nearestDay - ((int)(SpyglassAstronomyClient.getDay()%((long)period)));
                if (inDays < 0) inDays += 8;
                builder.append(" (In "+Integer.toString(inDays)+(inDays == 1 ? " Day)" : " Days)"));
            } else {
                builder.append("Always");
            }
        } else {
            flags[0] = true;
        }
    }

    private static void orbitInfo(StringBuilder builder, Orbit orbit, boolean[] flags) {
        if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.NOVICE)) {
            builder.append("Period: ");
            prettyFloat(builder, orbit.period);
            builder.append(" Days");
        } else {
            flags[0] = true;
        }

        boolean isEarth = orbit == SpyglassAstronomyClient.earthOrbit;

        if (!isEarth) {
            if (SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT)) {
                float max = Math.max(orbit.period, SpyglassAstronomyClient.earthOrbit.period);
                float min = Math.min(orbit.period, SpyglassAstronomyClient.earthOrbit.period);
                // max/min is often 1 less iteration for getDenominator
                double[] fraction = getFraction(max/min);

                builder.append("\nResonance: ");
                prettyFloat(builder, ((float)fraction[1]*max)/(float)(fraction[0] - fraction[1]));
                builder.append(" Days");
            } else {
                flags[0] = true;
            }
        }

        if ((isEarth && SpyglassAstronomyClient.knowledge.starKnowledgeAtleast(Level.ADEPT)) || SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.EXPERT)) {
            builder.append("\nCurrent position in orbit: ");
            builder.append((int)(orbit.lastLocalTime*100));
            builder.append("%");
        } else {
            flags[isEarth ? 0 : 1] = true;
        }

        if (!isEarth) {
            if (SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.EXPERT)) {
                builder.append("\nDistance: ");
                Vec3f pos = SpyglassAstronomyClient.earthOrbit.getLastRotatedPosition();
                pos.subtract(orbit.getLastRotatedPosition());
                float sqrDistance = SpyglassAstronomyClient.getSquaredDistance(pos.getX(), pos.getY(), pos.getZ());
                prettyFloat(builder, MathHelper.sqrt(sqrDistance));
                builder.append(" AU");
            } else {
                flags[1] = true;
            }
        }

        if (SpyglassAstronomyClient.knowledge.orbitKnowledgeAtleast(Level.MASTER)) {
            builder.append("\nEccentricity: ");
            prettyFloat(builder, orbit.eccentricity);

            builder.append("\nInclination: ");
            prettyFloat(builder, orbit.inclination);
        } else {
            flags[1] = true;
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

    private static void prettyFloat(StringBuilder builder, float f) {
        if (f == MathHelper.floor(f)) {
            builder.append((int)f);
        } else {
            f = Math.round(f*100);
            builder.append(f/100);
        }        
    }

    //https://github.com/Iru21/TimeDisplay/blob/master/src/main/kotlin/me/iru/timedisplay/TimeUtils.kt
    private static void getMinecraftTime(StringBuilder builder) {
        Long timeDay = SpyglassAstronomyClient.world.getTimeOfDay();
        int dayTicks = (int)(timeDay % 24000);
        int hour = (dayTicks / 1000 + 6) % 24;
        int min = ((int)(dayTicks / 16.666666f)) % 60;
        int sec = ((int)(dayTicks / 0.277777f)) % 60;
        builder.append(formatTime(hour, min, sec));
    }

    private static String formatTime(int hour, int min, int sec) {
        String time = Integer.toString(sec);
        if (time.length() == 1) time = "0"+time;
        time = Integer.toString(min) + ":" + time;
        if (time.length() == 4) time ="0"+time;
        time = Integer.toString(hour) + ":" + time;
        if (time.length() == 7) time ="0"+time;
        return time;   
    }
}
