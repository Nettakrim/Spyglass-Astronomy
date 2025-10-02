package com.nettakrim.spyglass_astronomy;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;

import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector4f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

public class SpaceRenderingManager {
    private final RenderSystem.ShapeIndexBuffer indexBuffer;

    private GpuBuffer starsBuffer;
    private int starsCount = 0;

    private GpuBuffer constellationsBuffer;
    private int constellationsCount = 0;

    private GpuBuffer drawingBuffer;
    private int drawingCount = 0;

    private GpuBuffer planetsBuffer;
    private int planetsCount = 0;

    private static float heightScale = 1;

    public static boolean constellationsVisible;
	public static boolean starsVisible;
    public static boolean orbitingBodiesVisible;
    public static boolean oldStarsVisible;
    public static boolean starsAlwaysVisible;

    private float starVisibility;

    private boolean constellationsNeedsUpdate = true;

    private File data = null;
    private final Path storagePath;
    private final String fileName;

    private static final RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET).withLocation("pipeline/sga_stars")
            .withVertexShader("core/position_color").withFragmentShader("core/position_color")
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.OVERLAY).withDepthWrite(false).build();

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

        indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
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
            if (player == null || SpyglassAstronomyClient.isntHoldingSpyglass()) {
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
            if (player == null || SpyglassAstronomyClient.isntHoldingSpyglass()) {
                Star.deselect();
            }            
        }

        if (OrbitingBody.selected != null) {
            ClientPlayerEntity player = SpyglassAstronomyClient.client.player;
            if (player == null || SpyglassAstronomyClient.isntHoldingSpyglass()) {
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
        drawingCount = 0;
    }

    private void updateConstellations() {
        drawingCount = SpyglassAstronomyClient.isDrawingConstellation ? 1 : 0;

        if (SpyglassAstronomyClient.constellations.isEmpty()) {
            constellationsCount = 0;
            return;
        }

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            constellation.setVertices(bufferBuilder, false);
        }

        BuiltBuffer builtBuffer = bufferBuilder.end();
        if (constellationsBuffer != null) {
            constellationsBuffer.close();
        }
        constellationsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Constellations Buffer", 40, builtBuffer.getBuffer());
        constellationsCount = builtBuffer.getDrawParameters().indexCount();
        builtBuffer.close();
    }

    private void updateStars(int ticks) {
        if (SpyglassAstronomyClient.stars.isEmpty()) {
            starsCount = 0;
            return;
        }

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Star star : SpyglassAstronomyClient.stars) {
            star.update(ticks);
            star.setVertices(bufferBuilder);
        }

        if (starsBuffer != null) {
            starsBuffer.close();
        }
        BuiltBuffer builtBuffer = bufferBuilder.end();
        starsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Stars Buffer", 40, builtBuffer.getBuffer());
        starsCount = builtBuffer.getDrawParameters().indexCount();
        builtBuffer.close();
    }

    private void updateOrbits(int ticks) {
        if (SpyglassAstronomyClient.orbitingBodies.isEmpty()) {
            orbitingBodiesVisible = false;
            return;
        }

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Long day = SpyglassAstronomyClient.getDay();
        float dayFraction = SpyglassAstronomyClient.getDayFraction();

        Vector3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
        Vector3f normalisedReferencePosition = new Vector3f(referencePosition);
        normalisedReferencePosition.normalize();

        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            orbitingBody.update(ticks, referencePosition, normalisedReferencePosition, day, dayFraction);
            orbitingBody.setVertices(bufferBuilder);
        }

        BuiltBuffer builtBuffer = bufferBuilder.end();
        if (planetsBuffer != null) {
            planetsBuffer.close();
        }
        planetsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Planets Buffer", 40, builtBuffer.getBuffer());
        planetsCount = builtBuffer.getDrawParameters().indexCount();
        builtBuffer.close();
    }

    private void updateDrawingConstellation() {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        SpyglassAstronomyClient.drawingConstellation.setVertices(bufferBuilder, true);

        BuiltBuffer builtBuffer = bufferBuilder.end();
        if (drawingBuffer != null) {
            drawingBuffer.close();
        }
        drawingBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Drawing Buffer", 40, builtBuffer.getBuffer());
        drawingCount = builtBuffer.getDrawParameters().indexCount();
        builtBuffer.close();
    }

    public void render(MatrixStack matrices, SkyRenderState skyRenderState) {
        starVisibility = starsAlwaysVisible ? 1 : skyRenderState.starBrightness;
        if (starVisibility > 0) {
            float colorScale = starVisibility+Math.min(heightScale, 0.5f);

            if (constellationsVisible && SpyglassAstronomyClient.isDrawingConstellation || drawingCount > 0) {
                updateDrawingConstellation();
            }

            GpuTextureView mainColor = MinecraftClient.getInstance().getFramebuffer().getColorAttachmentView();
            GpuTextureView mainDepth = MinecraftClient.getInstance().getFramebuffer().getDepthAttachmentView();
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();

            if (starsVisible || constellationsVisible) {
                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(SpyglassAstronomyClient.getStarAngle()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45f));

                matrix4fStack.pushMatrix();
                matrix4fStack.mul(matrices.peek().getPositionMatrix());

                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(colorScale, colorScale, colorScale, starVisibility), new Vector3f(), new Matrix4f(), 0.0F);
                RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", mainColor, OptionalInt.empty(), mainDepth, OptionalDouble.empty());
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

                try {
                    renderPass.setPipeline(pipeline);

                    if (starsVisible) {
                        draw(renderPass, starsBuffer, starsCount);
                    }

                    if (constellationsVisible) {
                        draw(renderPass, constellationsBuffer, constellationsCount);

                        if (SpyglassAstronomyClient.isDrawingConstellation) {
                            draw(renderPass, drawingBuffer, drawingCount);
                        }
                    }
                } catch (Throwable ignored) {}

                renderPass.close();
                matrices.pop();
                matrix4fStack.popMatrix();
            }

            if (orbitingBodiesVisible && planetsCount > 0) {
                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(SpyglassAstronomyClient.getPositionInOrbit(360f) * (1 - 1 / SpyglassAstronomyClient.earthOrbit.period) + 180));

                matrix4fStack.pushMatrix();
                matrix4fStack.mul(matrices.peek().getPositionMatrix());

                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().write(matrix4fStack, new Vector4f(colorScale, colorScale, colorScale, starVisibility), new Vector3f(), new Matrix4f(), 0.0F);
                RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Planets", mainColor, OptionalInt.empty(), mainDepth, OptionalDouble.empty());
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

                try {
                    renderPass.setPipeline(pipeline);
                    draw(renderPass, planetsBuffer, planetsCount);
                } catch (Throwable ignored) {}

                renderPass.close();
                matrices.pop();
                matrix4fStack.popMatrix();
            }
        }
    }

    public static void assignIrisPipeline() {
        try {
            String irisApiPackage = "net.irisshaders.iris.api.v0.";

            Class<?> irisApiClass = Class.forName(irisApiPackage + "IrisApi");
            Object INSTANCE = irisApiClass.getMethod("getInstance").invoke(null);

            Class<?> irisProgramClass = Class.forName(irisApiPackage + "IrisProgram");
            Enum<?> SKY_BASIC = Enum.valueOf(irisProgramClass.asSubclass(Enum.class), "SKY_BASIC");

            Method assignPipeline = irisApiClass.getMethod("assignPipeline", RenderPipeline.class, irisProgramClass);
            assignPipeline.invoke(INSTANCE, pipeline, SKY_BASIC);
        } catch (Exception ignored) {
            SpyglassAstronomyClient.LOGGER.error("Failed to assign pipeline. Shader compatibility may be broken");
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

    private void draw(RenderPass renderPass, GpuBuffer gpuBuffer, int count) {
        if (count == 0) return;

        renderPass.setVertexBuffer(0, gpuBuffer);
        renderPass.setIndexBuffer(indexBuffer.getIndexBuffer(count), indexBuffer.getIndexType());
        renderPass.drawIndexed(0,0, count, 1);
    }
}
