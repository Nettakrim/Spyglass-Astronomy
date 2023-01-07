package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
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

    private VertexBuffer orbitingBodiesBuffer = new VertexBuffer();
    private BufferBuilder orbitingBodiesBufferBuilder = Tessellator.getInstance().getBuffer();

    private static float heightScale = 1;
    private static float unclampedHeightScale = 1;

    public void updateSpace(int ticks) {
        updateHeightScale();
        if (Constellation.selected != null) {
            ClientPlayerEntity player = SpyglassAstronomyClient.client.player;
            if (player == null || !SpyglassAstronomyClient.isHoldingSpyglass()) {
                Constellation.deselect();
                constellationsNeedsUpdate = true;
            }
        }
        if (constellationsNeedsUpdate) {
            updateConstellations();
            constellationsNeedsUpdate = false;
        }

        if (Star.selected != null) {
            ClientPlayerEntity player = SpyglassAstronomyClient.client.player;
            if (player == null || !SpyglassAstronomyClient.isHoldingSpyglass()) {
                Star.deselect();
            }            
        }

        updateStars(ticks);

        updateOrbits();
    }

    public void scheduleConstellationsUpdate() {
        constellationsNeedsUpdate = true;
    }

    private void updateConstellations() {
        constellationsBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            constellation.setVertices(constellationsBufferBuilder, false);
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

    private void updateOrbits() {
        orbitingBodiesBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float t = SpyglassAstronomyClient.getPreciseDay();

        Vec3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(t);

        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            orbitingBody.setVertices(constellationsBufferBuilder, referencePosition, t);
        }

        orbitingBodiesBuffer.bind();
        orbitingBodiesBuffer.upload(orbitingBodiesBufferBuilder.end());
    }

    private void updateDrawingConstellation() {
        drawingConstellationsBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        SpyglassAstronomyClient.drawingConstellation.setVertices(drawingConstellationsBufferBuilder, true);

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

            matrices.pop();
            matrices.push();
            
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((SpyglassAstronomyClient.getPreciseDay()/SpyglassAstronomyClient.earthOrbit.period)*-360f));
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((SpyglassAstronomyClient.getPreciseDay()*360f)+180));

            orbitingBodiesBuffer.bind();
            orbitingBodiesBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();

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
