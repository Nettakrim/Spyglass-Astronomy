package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

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
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 1, target="Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V")
    )
	private void stopStarRender(VertexBuffer buffer, Matrix4f positionMatrix, Matrix4f projectionMatrix, ShaderProgram positionShader) {
        if (SpaceRenderingManager.oldStarsVisible) {
            buffer.draw(positionMatrix, projectionMatrix, positionShader);
        }
    }

    @Inject(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 0, target="Lnet/minecraft/client/world/ClientWorld;method_23787(F)F")
    )
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.Render(matrices, projectionMatrix, tickDelta, camera, bl, runnable);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }
}
