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

    private VertexBuffer planetsBuffer = new VertexBuffer();
    private BufferBuilder planetsBufferBuilder = Tessellator.getInstance().getBuffer();

    private static float heightScale = 1;

    public static boolean constellationsVisible;
	public static boolean starsVisible;
    public static boolean orbitingBodiesVisible;
    public static boolean oldStarsVisible;

    public SpaceRenderingManager() {
        constellationsVisible = true;
        starsVisible = true;
        orbitingBodiesVisible = true;
        oldStarsVisible = false;
    }

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

        updateOrbits(ticks);
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

    private void updateOrbits(int ticks) {
        planetsBufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Long day = SpyglassAstronomyClient.getDay();
        float dayFraction = SpyglassAstronomyClient.getDayFraction();

        Vec3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(day, dayFraction);
        Vec3f normalisedReferencePosition = referencePosition.copy();
        normalisedReferencePosition.normalize();

        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            orbitingBody.update(ticks, referencePosition, normalisedReferencePosition, day, dayFraction);
            orbitingBody.setVertices(constellationsBufferBuilder);
        }

        planetsBuffer.bind();
        planetsBuffer.upload(planetsBufferBuilder.end());
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
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(SpyglassAstronomyClient.getStarAngle()));
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(45f));
            float colorScale = starVisibility+Math.min(heightScale, 0.5f);
            RenderSystem.setShaderColor(colorScale, colorScale, colorScale, starVisibility);
            BackgroundRenderer.clearFog();
            
            if (starsVisible) {
                starsBuffer.bind();
                starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();
            }

            if (constellationsVisible) {
                constellationsBuffer.bind();
                constellationsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();
                if (SpyglassAstronomyClient.isDrawingConstellation) {
                    updateDrawingConstellation();
                    drawingConstellationsBuffer.bind();
                    drawingConstellationsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
                }
            }

            if (orbitingBodiesVisible) {
                matrices.pop();
                matrices.push();
                matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(SpyglassAstronomyClient.getPositionInOrbit(360f)*(1-1/SpyglassAstronomyClient.earthOrbit.period)+180));

                planetsBuffer.bind();
                planetsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();
            }

            runnable.run();
        }
    }

    public static void updateHeightScale() {
        heightScale = MathHelper.clamp((SpyglassAstronomyClient.getHeight()-32f)/256f, 0f, 1f);
    }

    public static float getHeightScale() {
        return heightScale;
    }
}
