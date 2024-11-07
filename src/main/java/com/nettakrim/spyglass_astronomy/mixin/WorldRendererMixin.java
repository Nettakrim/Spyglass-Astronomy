package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow private int ticks;

    @WrapWithCondition(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", ordinal = 1, target="Lnet/minecraft/client/gl/VertexBuffer;draw(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/gl/ShaderProgram;)V")
    )
    private boolean stopStarRender(VertexBuffer buffer, Matrix4f positionMatrix, Matrix4f projectionMatrix, ShaderProgram positionShader) {
        return SpaceRenderingManager.oldStarsVisible;
    }

    @Inject(
            method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", ordinal = 4, target="Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V")
    )
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.Render(matrices, projectionMatrix, tickDelta);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void updateStars(CallbackInfo ci) {
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace(ticks);
    }
}
