package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private int ticks;

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SkyRendering;renderCelestialBodies(Lnet/minecraft/client/util/math/MatrixStack;FIFF)V"))
    public void renderSky(GpuBufferSlice gpuBufferSlice, SkyRenderState skyRenderState, CallbackInfo ci, @Local MatrixStack matrices) {
        SpyglassAstronomyClient.spaceRenderingManager.render(matrices, skyRenderState);
    }
}
