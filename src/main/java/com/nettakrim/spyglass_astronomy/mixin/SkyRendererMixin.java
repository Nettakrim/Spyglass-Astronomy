package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
	@WrapWithCondition(
        method = "renderSunMoonAndStars",
        at = @At(value = "INVOKE", target= "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
	private boolean stopStarRender(SkyRenderer instance, float starBrightness, PoseStack poseStack) {
        return SpaceRenderingManager.oldStarsVisible;
    }
}
