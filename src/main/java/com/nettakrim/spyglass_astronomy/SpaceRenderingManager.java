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
import net.minecraft.util.math.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;


public class SpaceRenderingManager {
    private VertexBuffer starsBuffer = new VertexBuffer();
    private BufferBuilder starBufferBuilder = Tessellator.getInstance().getBuffer();

    private VertexBuffer constellationsBuffer = new VertexBuffer();
    private BufferBuilder constellationsBufferBuilder = Tessellator.getInstance().getBuffer();
    private boolean constellationsNeedsUpdate = true;

    private VertexBuffer drawingConstellationsBuffer = new VertexBuffer();
    private BufferBuilder drawingConstellationsBufferBuilder = Tessellator.getInstance().getBuffer();

    public void UpdateSpace(int ticks) {
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
            matrices.push();
            //matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90.0f));
            //matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getPreciseMoonPhase()*45.0f));
            //matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(45f));
            RenderSystem.setShaderColor(starVisibility, starVisibility, starVisibility, starVisibility);
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
            matrices.pop();
        }
    }
}
