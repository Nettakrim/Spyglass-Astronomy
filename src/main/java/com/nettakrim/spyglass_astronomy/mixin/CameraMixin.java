package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.minecraft.client.Camera;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow private float fovModifier;
    @Shadow private float oldFovModifier;
    @Inject(
            method = "tickFov",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updateFovMultiplier(CallbackInfo ci) {
        if (SpyglassAstronomyClient.zoom == 0) return;

        if (!(SpyglassAstronomyClient.client.player.isScoping() && SpyglassAstronomyClient.client.options.getCameraType().isFirstPerson())) SpyglassAstronomyClient.zoom = 0;

        float f = 1.0f;
        if (SpyglassAstronomyClient.client.getCameraEntity() instanceof AbstractClientPlayer abstractClientPlayerEntity) {
            f = abstractClientPlayerEntity.getFieldOfViewModifier(true, 0);
        }
        //1.25892541179 would be more accurate, but it doesnt really matter
        f *= (float)Math.pow(1.25d, SpyglassAstronomyClient.zoom);
        this.oldFovModifier = this.fovModifier;
        this.fovModifier += (f - this.fovModifier) * 0.5f;
        if (this.fovModifier > 1.5f) {
            this.fovModifier = 1.5f;
        }
        if (this.fovModifier < 0.01f) {
            this.fovModifier = 0.01f;
        }

        ci.cancel();
    }
}
