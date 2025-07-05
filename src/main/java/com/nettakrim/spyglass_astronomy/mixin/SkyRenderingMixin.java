package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRendering.class)
public class SkyRenderingMixin {
	@WrapWithCondition(
        method = "renderCelestialBodies",
        at = @At(value = "INVOKE", target="Lnet/minecraft/client/render/SkyRendering;renderStars(FLnet/minecraft/client/util/math/MatrixStack;)V")
    )
	private boolean stopStarRender(SkyRendering instance, float brightness, MatrixStack matrices) {
        return SpaceRenderingManager.oldStarsVisible;
    }
}
