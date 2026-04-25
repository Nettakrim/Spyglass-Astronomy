package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpaceRenderingManager;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import com.mojang.blaze3d.systems.RenderSystem;
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

@Mixin(value = WorldRenderer.class, priority = 2000)
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

    // Vanilla path (no shaders): inject at the original point
    @Inject(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 4, target="Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V")
    )
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (SpaceRenderingManager.isShadersActive()) return;
        SpyglassAstronomyClient.spaceRenderingManager.Render(matrices, projectionMatrix, tickDelta, camera, bl, runnable);
    }

    // Shader path is handled by WorldRenderEvents.LAST in SpyglassAstronomyClient,
    // which fires after Iris's composite passes so vertex colors reach the output framebuffer directly.

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }
}
