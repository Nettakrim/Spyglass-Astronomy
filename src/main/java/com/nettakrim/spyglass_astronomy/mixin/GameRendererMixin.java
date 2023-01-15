package com.nettakrim.spyglass_astronomy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

//https://github.com/Nova-Committee/AbsolutelyNotAZoomMod/blob/fabric/universal/src/main/java/committee/nova/anazm/mixin/GameRendererMixin.java

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyVariable(
        method = "getFov",
        at = @At(
            value = "RETURN",
            shift = At.Shift.BEFORE
        ), ordinal = 0
    )
    private double getFov(double fov) {
        if (SpyglassAstronomyClient.zoom == 0) return fov;

        if (!SpyglassAstronomyClient.client.player.isUsingSpyglass()) SpyglassAstronomyClient.zoom = 0;

        float z = (float)Math.pow(1.25d, SpyglassAstronomyClient.zoom);
        return fov * z;
    }
}
