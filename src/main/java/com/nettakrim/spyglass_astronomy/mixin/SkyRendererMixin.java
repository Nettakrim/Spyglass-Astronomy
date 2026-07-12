package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;

import com.mojang.blaze3d.vertex.PoseStack;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @Shadow @Final
    private TextureAtlas celestialsAtlas;

    @WrapWithCondition(
        method = "renderSunMoonAndStars",
        at = @At(value = "INVOKE", target= "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
	private boolean stopStarRender(SkyRenderer instance, float starBrightness, PoseStack poseStack) {
        return SpaceRenderingManager.oldStarsVisible;
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
    private void renderCustomStars(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.render(poseStack, starBrightness, celestialsAtlas);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closeBuffers(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.close();
    }
}
