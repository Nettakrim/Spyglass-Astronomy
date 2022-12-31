package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow private int ticks;

	@Redirect(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 1, target="Lnet/minecraft/client/gl/VertexBuffer;draw(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V")
    )
	private void stopStarRender(VertexBuffer buffer, Matrix4f positionMatrix, Matrix4f projectionMatrix, Shader positionShader) {
        return;
    }

    @Inject(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 0, target="Lnet/minecraft/client/world/ClientWorld;method_23787(F)F")
    )
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.Render(matrices, projectionMatrix, tickDelta, camera, bl, runnable);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.UpdateSpace(ticks);
    }
}
