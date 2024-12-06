package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private int ticks;

    @Unique
    private float tickDelta;

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void setTickDelta(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        this.tickDelta = tickDelta;
    }

    @Inject(method = "method_62215", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SkyRendering;renderCelestialBodies(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;FIFFLnet/minecraft/client/render/Fog;)V"))
    public void renderSky(Fog fog, DimensionEffects.SkyType skyType, float f, DimensionEffects dimensionEffects, CallbackInfo ci, @Local MatrixStack matrices) {
        SpyglassAstronomyClient.spaceRenderingManager.render(matrices, RenderSystem.getProjectionMatrix(), tickDelta);
    }
}
