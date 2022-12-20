package com.nettakrim.spyglass_astronomy.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Redirect(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 1, target="Lnet/minecraft/client/gl/VertexBuffer;draw(Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/Shader;)V")
    )
	private void stopStarRender(VertexBuffer buffer, Matrix4f positionMatrix, Matrix4f projectionMatrix, Shader positionShader) {
        return;
    }

    @Inject(
        method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
        at = @At(value = "INVOKE", ordinal = 0, target="Lcom/mojang/blaze3d/systems/RenderSystem;defaultBlendFunc()V")
    )
    public void renderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (SpyglassAstronomyClient.starsBuffer == null) {
            SpyglassAstronomyClient.CreateStarBuffer(10L);
        }
        float starVisibility = SpyglassAstronomyClient.world.method_23787(tickDelta) * (1.0f - SpyglassAstronomyClient.world.getRainGradient(tickDelta));
        if (starVisibility > 0) {
            matrices.push();
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getPreciseMoonPhase()*45.0f));
            RenderSystem.setShaderColor(0, starVisibility, starVisibility, starVisibility);
            BackgroundRenderer.clearFog();
            SpyglassAstronomyClient.starsBuffer.bind();
            SpyglassAstronomyClient.starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionShader());
            VertexBuffer.unbind();
            runnable.run();
            matrices.pop();
        }
    }
}
