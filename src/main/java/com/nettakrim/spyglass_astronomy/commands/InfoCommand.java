package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.Constellation;
import com.nettakrim.spyglass_astronomy.Orbit;
import com.nettakrim.spyglass_astronomy.OrbitingBody;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import com.nettakrim.spyglass_astronomy.Star;

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
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(constellation.name);
        builder.append('\n');

        Vec3f position = constellation.getAveragePosition();
        mostVisibleInfo(builder, position);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayInfo(Star star) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(star.name == null ? "Unnamed" : star.name);
        builder.append('\n');

        Vec3f position = star.getPositionAsVec3f();
        mostVisibleInfo(builder, position);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayInfo(OrbitingBody orbitingBody) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(orbitingBody.name == null ? "Unnamed" : orbitingBody.name);
        builder.append('\n');

        orbitInfo(builder, orbitingBody.orbit);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayEarthInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("Time: ");
        getMinecraftTime(builder);
        builder.append('\n');

        orbitInfo(builder, SpyglassAstronomyClient.earthOrbit);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void mostVisibleInfo(StringBuilder builder, Vec3f position) {
        position.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        position.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(1.75f * 405f));
        position.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45f));
        if (MathHelper.abs(position.getX()) < 0.9f) {
            float angle = (float)(MathHelper.atan2(position.getY(), position.getZ())*180d/Math.PI);
            int mostVisiblePhase = Math.round(angle/45)-1;
            if (mostVisiblePhase < 0) mostVisiblePhase += 8;
            builder.append("Most Visible During: ");
            builder.append(SpyglassAstronomyClient.getMoonPhaseName(mostVisiblePhase));
        } else {
            builder.append("Always");
        }        
    }

    private static void orbitInfo(StringBuilder builder, Orbit orbit) {
        builder.append("Period: ");
        prettyFloat(builder, orbit.period);
        builder.append(" Days");

        builder.append('\n');
        builder.append("Eccentricity: ");
        prettyFloat(builder, orbit.eccentricity);

        builder.append('\n');
        builder.append("Inclination: ");
        prettyFloat(builder, orbit.inclination);
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
