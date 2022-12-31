package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import com.mojang.blaze3d.systems.RenderSystem;


public class SpaceRenderingManager {
    private VertexBuffer starsBuffer = new VertexBuffer();
    private BufferBuilder starBufferBuilder = Tessellator.getInstance().getBuffer();

    private VertexBuffer constellationsBuffer = new VertexBuffer();
    private BufferBuilder constellationsBufferBuilder = Tessellator.getInstance().getBuffer();
    private boolean constellationsNeedsUpdate = true;

    private VertexBuffer drawingConstellationsBuffer = new VertexBuffer();
    private BufferBuilder drawingConstellationsBufferBuilder = Tessellator.getInstance().getBuffer();

    private static float heightScale = 1;
    private static float unclampedHeightScale = 1;

    public void UpdateSpace(int ticks) {
        updateHeightScale();
        if (constellationsNeedsUpdate) {
            updateConstellations();
            constellationsNeedsUpdate = false;
        }
        updateStars(ticks);
    }

    public void scheduleConstellationsUpdate() {
        constellationsNeedsUpdate = true;
    }

    private void updateConstellations() {
        constellationsBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            constellation.render(constellationsBufferBuilder);
        }

        constellationsBuffer.bind();
        constellationsBuffer.upload(constellationsBufferBuilder.end());
    }

    private void updateStars(int ticks) {
        starBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Star star : SpyglassAstronomyClient.stars) {
            star.update(ticks);
            star.setVertices(starBufferBuilder);
        }

        starsBuffer.bind();
        starsBuffer.upload(starBufferBuilder.end());
    }

    private void updateDrawingConstellation() {
        drawingConstellationsBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        SpyglassAstronomyClient.drawingConstellation.render(drawingConstellationsBufferBuilder);

        drawingConstellationsBuffer.bind();
        drawingConstellationsBuffer.upload(drawingConstellationsBufferBuilder.end());
    }

    public void Render(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable) {
        float starVisibility = SpyglassAstronomyClient.world.method_23787(tickDelta) * (1.0f - SpyglassAstronomyClient.world.getRainGradient(tickDelta));
        if (starVisibility > 0) {
            matrices.pop();
            matrices.push();
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getPreciseMoonPhase()*405f));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(45f));
            float colorScale = starVisibility+Math.min(heightScale, 0.5f);
            RenderSystem.setShaderColor(colorScale, colorScale, colorScale, Math.min(starVisibility*((unclampedHeightScale*MathHelper.abs(unclampedHeightScale)+2))/2,1));
            BackgroundRenderer.clearFog();
            
            starsBuffer.bind();
            starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();

            constellationsBuffer.bind();
            constellationsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();

            if (SpyglassAstronomyClient.isDrawingConstellation) {
                updateDrawingConstellation();
                drawingConstellationsBuffer.bind();
                drawingConstellationsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
            }

            runnable.run();
        }
    }

    public static void updateHeightScale() {
        unclampedHeightScale = (SpyglassAstronomyClient.getHeight()-32f)/256f;
        heightScale = MathHelper.clamp(unclampedHeightScale, 0f, 1f);
    }

    public static float getHeightScale() {
        return heightScale;
    }
}