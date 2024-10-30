package com.nettakrim.spyglass_astronomy;

import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class SpaceRenderingManager {
    private final VertexBuffer starsBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    private boolean starsReady = false;

    private final VertexBuffer constellationsBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    private boolean constellationsReady = false;

    private final VertexBuffer drawingConstellationsBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    private boolean drawingReady = false;

    private final VertexBuffer planetsBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
    private boolean planetsReady = false;

    private static float heightScale = 1;

    public static boolean constellationsVisible;
	public static boolean starsVisible;
    public static boolean orbitingBodiesVisible;
    public static boolean oldStarsVisible;
    public static boolean starsAlwaysVisible;

    private float starVisibility;

    private boolean constellationsNeedsUpdate = true;

    private File data = null;
    private Path storagePath;
    private final String fileName;

    public SpaceRenderingManager() {
        storagePath = SpyglassAstronomyClient.client.runDirectory.toPath().resolve(".spyglass_astronomy");

        fileName = storagePath +"/rendering.txt";

        constellationsVisible = true;
        starsVisible = true;
        orbitingBodiesVisible = true;
        oldStarsVisible = false;
        starsAlwaysVisible = false;

        if (Files.exists(storagePath)) {
            data = new File(fileName);
            if (data.exists()) {
                loadData();
            }
        }
    }

    private void loadData() {
        try {
            if (data.createNewFile()) {
                return;
            }
            Scanner scanner = new Scanner(data);
            String s = scanner.nextLine();
            scanner.close();
            constellationsVisible = charTrue(s, 0);
            starsVisible = charTrue(s, 1);
            orbitingBodiesVisible = charTrue(s, 2);
            oldStarsVisible = charTrue(s, 3);
            starsAlwaysVisible = charTrue(s, 4);
        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to load data");
        }
    }

    private boolean charTrue(String s, int index) {
        return index < s.length() && s.charAt(index) == '1';
    }

    public void saveData() {
        try {
            if (data == null) {
                Files.createDirectories(storagePath);
                data = new File(fileName);
            }
            FileWriter writer = new FileWriter(data);
            String s = (constellationsVisible ? "1" : "0") + (starsVisible ? "1" : "0") + (orbitingBodiesVisible ? "1" : "0") + (oldStarsVisible ? "1" : "0") + (starsAlwaysVisible ? "1" : "0");
            writer.write(s);
            writer.close();

        } catch (IOException e) {
            SpyglassAstronomyClient.LOGGER.info("Failed to save data");
        }
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

        if (OrbitingBody.selected != null) {
            ClientPlayerEntity player = SpyglassAstronomyClient.client.player;
            if (player == null || !SpyglassAstronomyClient.isHoldingSpyglass()) {
                OrbitingBody.deselect();
            }               
        }

        updateStars(ticks);

        updateOrbits(ticks);
    }

    public void scheduleConstellationsUpdate() {
        constellationsNeedsUpdate = true;
    }

    public void cancelDrawing() {
        drawingReady = false;
    }

    private void updateConstellations() {
        drawingReady = SpyglassAstronomyClient.isDrawingConstellation;

        if (SpyglassAstronomyClient.constellations.isEmpty()) {
            constellationsReady = false;
            return;
        }

        BufferBuilder constellationsBufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            constellation.setVertices(constellationsBufferBuilder, false);
        }

        constellationsBuffer.bind();
        constellationsBuffer.upload(constellationsBufferBuilder.end());
        constellationsReady = true;
    }

    private void updateStars(int ticks) {
        if (SpyglassAstronomyClient.stars.isEmpty()) {
            starsReady = false;
            return;
        }

        BufferBuilder starBufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Star star : SpyglassAstronomyClient.stars) {
            star.update(ticks);
            star.setVertices(starBufferBuilder);
        }

        starsBuffer.bind();
        starsBuffer.upload(starBufferBuilder.end());
        starsReady = true;
    }

    private void updateOrbits(int ticks) {
        if (SpyglassAstronomyClient.orbitingBodies.isEmpty()) {
            orbitingBodiesVisible = false;
            return;
        }

        BufferBuilder planetsBufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Long day = SpyglassAstronomyClient.getDay();
        float dayFraction = SpyglassAstronomyClient.getDayFraction();

        Vector3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
        Vector3f normalisedReferencePosition = new Vector3f(referencePosition);
        normalisedReferencePosition.normalize();

        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            orbitingBody.update(ticks, referencePosition, normalisedReferencePosition, day, dayFraction);
            orbitingBody.setVertices(planetsBufferBuilder);
        }

        planetsBuffer.bind();
        planetsBuffer.upload(planetsBufferBuilder.end());
        planetsReady = true;
    }

    private void updateDrawingConstellation() {
        BufferBuilder drawingConstellationsBufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        SpyglassAstronomyClient.drawingConstellation.setVertices(drawingConstellationsBufferBuilder, true);

        drawingConstellationsBuffer.bind();
        drawingConstellationsBuffer.upload(drawingConstellationsBufferBuilder.end());
    }

    public void render(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta) {
        starVisibility = starsAlwaysVisible ? 1 : SpyglassAstronomyClient.world.getStarBrightness(tickDelta) * (1.0f - SpyglassAstronomyClient.world.getRainGradient(tickDelta));
        if (starVisibility > 0) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(SpyglassAstronomyClient.getStarAngle()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45f));

            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.mul(matrices.peek().getPositionMatrix());

            float colorScale = starVisibility+Math.min(heightScale, 0.5f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.setShaderColor(colorScale, colorScale, colorScale, starVisibility);

            RenderSystem.depthMask(false);
            RenderSystem.overlayBlendFunc();
            RenderSystem.enableBlend();

            if (starsVisible && starsReady) {
                starsBuffer.bind();
                starsBuffer.draw(matrix4fStack, projectionMatrix, RenderSystem.getShader());
                VertexBuffer.unbind();
            }

            if (constellationsVisible) {
                if (constellationsReady) {
                    constellationsBuffer.bind();
                    constellationsBuffer.draw(matrix4fStack, projectionMatrix, RenderSystem.getShader());
                    VertexBuffer.unbind();
                }
                if (SpyglassAstronomyClient.isDrawingConstellation || drawingReady) {
                    updateDrawingConstellation();
                    drawingConstellationsBuffer.bind();
                    drawingConstellationsBuffer.draw(matrix4fStack, projectionMatrix, RenderSystem.getShader());
                }
            }

            if (orbitingBodiesVisible && planetsReady) {
                matrices.pop();
                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(SpyglassAstronomyClient.getPositionInOrbit(360f)*(1-1/SpyglassAstronomyClient.earthOrbit.period)+180));

                matrix4fStack.popMatrix();
                matrix4fStack.pushMatrix();
                matrix4fStack.mul(matrices.peek().getPositionMatrix());

                planetsBuffer.bind();
                planetsBuffer.draw(matrix4fStack, projectionMatrix, RenderSystem.getShader());
                VertexBuffer.unbind();
            }

            matrices.pop();
            matrix4fStack.popMatrix();

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    public static void updateHeightScale() {
        heightScale = MathHelper.clamp((SpyglassAstronomyClient.getHeight()-32f)/256f, 0f, 1f);
    }

    public static float getHeightScale() {
        return heightScale;
    }

    public boolean starsCurrentlyVisible() {
        return starVisibility > 0;
    }
}
