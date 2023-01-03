package com.nettakrim.spyglass_astronomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nettakrim.spyglass_astronomy.Constellation;
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

    private static void displayInfo(Constellation constellation) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(constellation.name);

        Vec3f position = constellation.getAveragePosition();
        mostVisibleInfo(builder, position);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void displayInfo(Star star) {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(star.name == null ? "Unnamed" : star.name);

        Vec3f position = star.getPositionAsVec3f();
        mostVisibleInfo(builder, position);

        SpyglassAstronomyClient.longSay(builder.toString());
    }

    private static void mostVisibleInfo(StringBuilder builder, Vec3f position) {
        position.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        position.rotate(Vec3f.POSITIVE_X.getDegreesQuaternion(1.75f * 405f));
        position.rotate(Vec3f.POSITIVE_Y.getDegreesQuaternion(-45f));
        builder.append('\n');
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
}
