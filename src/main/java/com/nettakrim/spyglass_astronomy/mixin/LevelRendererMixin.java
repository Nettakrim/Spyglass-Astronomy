package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 2000)
public class LevelRendererMixin {
    @Shadow
    private int ticks;

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }

    // Vanilla path (no shaders): inject at the original point
    @Inject(method = "lambda$addSkyPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSunMoonAndStars(Lcom/mojang/blaze3d/vertex/PoseStack;FFFLnet/minecraft/world/level/MoonPhase;FF)V"))
    private static void renderSky(GpuBufferSlice skyFog, SkyRenderState state, SkyRenderer skyRenderer, CallbackInfo ci, @Local(name = "poseStack") PoseStack poseStack) {
        if (SpaceRenderingManager.isShadersActive()) {
            SpaceRenderingManager.captureState(state, poseStack);
            return;
        }
        SpyglassAstronomyClient.spaceRenderingManager.render(poseStack, state);
    }

    // Shader path is handled by WorldRenderEvents.LAST in SpyglassAstronomyClient,
    // which fires after Iris's composite passes so vertex colors reach the output framebuffer directly.
}
