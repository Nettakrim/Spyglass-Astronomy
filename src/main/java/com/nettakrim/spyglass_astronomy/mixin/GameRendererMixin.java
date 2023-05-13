package com.nettakrim.spyglass_astronomy.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.injection.Inject;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//https://github.com/Nova-Committee/AbsolutelyNotAZoomMod/blob/fabric/universal/src/main/java/committee/nova/anazm/mixin/GameRendererMixin.java

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow private float fovMultiplier;
    @Shadow private float lastFovMultiplier;
    @Inject(
        method = "updateFovMultiplier",
        at = @At("HEAD"),
        cancellable = true
    )
    private void updateFovMultiplier(CallbackInfo ci) {
        if (SpyglassAstronomyClient.zoom == 0) return;

        if (!(SpyglassAstronomyClient.client.player.isUsingSpyglass() && SpyglassAstronomyClient.client.options.getPerspective().isFirstPerson())) SpyglassAstronomyClient.zoom = 0;

        float f = 1.0f;
        if (SpyglassAstronomyClient.client.getCameraEntity() instanceof AbstractClientPlayerEntity abstractClientPlayerEntity) {
            f = abstractClientPlayerEntity.getFovMultiplier();
        }
        f *= (float)Math.pow(1.25d, SpyglassAstronomyClient.zoom);
        this.lastFovMultiplier = this.fovMultiplier;
        this.fovMultiplier += (f - this.fovMultiplier) * 0.5f;
        if (this.fovMultiplier > 1.5f) {
            this.fovMultiplier = 1.5f;
        }
        if (this.fovMultiplier < 0.01f) {
            this.fovMultiplier = 0.01f;
        }

        ci.cancel();
    }
}
