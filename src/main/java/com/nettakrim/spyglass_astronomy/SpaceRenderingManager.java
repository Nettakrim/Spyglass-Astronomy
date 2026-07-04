package com.nettakrim.spyglass_astronomy;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.MeshData;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

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
    private final RenderSystem.AutoStorageIndexBuffer indexBuffer;

    private GpuBuffer starsBuffer;
    private int starsCount = 0;

    private GpuBuffer constellationsBuffer;
    private int constellationsCount = 0;

    private GpuBuffer drawingBuffer;
    private int drawingCount = 0;

    private GpuBuffer planetsBuffer;
    private int planetsCount = 0;

    private static float heightScale = 1;


    private static Boolean shaderModLoaded = null;
    private static SkyRenderState skyRenderState;
    private static Matrix4f lastPose;

    private static boolean isShaderModLoaded() {
        if (shaderModLoaded == null) {
            try {
                Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
                Object loader = loaderClass.getMethod("getInstance").invoke(null);
                boolean iris = (boolean) loaderClass.getMethod("isModLoaded", String.class).invoke(loader, "iris");
                boolean oculus = (boolean) loaderClass.getMethod("isModLoaded", String.class).invoke(loader, "oculus");
                shaderModLoaded = iris || oculus;
            } catch (Exception e) {
                shaderModLoaded = false;
            }
        }
        return shaderModLoaded;
    }

    public static boolean isShadersActive() {
        if (!isShaderModLoaded()) return false;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method m = apiClass.getMethod("isShaderPackInUse");
            return (boolean) m.invoke(api);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRenderingShadowPass() {
        if (!isShaderModLoaded()) return false;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method m = apiClass.getMethod("isRenderingShadowPass");
            return (boolean) m.invoke(api);
        } catch (Exception e) {
            return false;
        }
    }

    public static void captureState(SkyRenderState skyRenderState, PoseStack poseStack) {
        SpaceRenderingManager.skyRenderState = skyRenderState;
        SpaceRenderingManager.lastPose = poseStack.last().pose();
    }

    // When a shader pack is active, render after Iris's composite passes so vertex colors
    // reach the output framebuffer directly instead of going through the deferred GBuffer pipeline.
    public static void lateRender(LevelRenderContext context) {
        if (!SpaceRenderingManager.isShadersActive()) return;
        if (SpaceRenderingManager.isRenderingShadowPass()) return;
        if (SpyglassAstronomyClient.spaceRenderingManager == null) return;
        PoseStack matrices = context.poseStack();
        matrices.pushPose();
        matrices.setIdentity();
        matrices.mulPose(lastPose);
        SpyglassAstronomyClient.spaceRenderingManager.render(matrices, skyRenderState);
        matrices.popPose();
        context.bufferSource().endBatch();
    }

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

    private static final RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/sga_stars")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .build();

    public SpaceRenderingManager() {
        storagePath = SpyglassAstronomyClient.client.gameDirectory.toPath().resolve(".spyglass_astronomy");

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

        indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    }

    private void loadData() {
        try {
            if (data.exists()) {
                Scanner scanner = new Scanner(data);
                String s = scanner.nextLine();
                scanner.close();
                constellationsVisible = charTrue(s, 0);
                starsVisible = charTrue(s, 1);
                orbitingBodiesVisible = charTrue(s, 2);
                oldStarsVisible = charTrue(s, 3);
                starsAlwaysVisible = charTrue(s, 4);
            }
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
                data = new File(fileName);
                storagePath.toFile().mkdirs();
                data.createNewFile();
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
            LocalPlayer player = SpyglassAstronomyClient.client.player;
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
            LocalPlayer player = SpyglassAstronomyClient.client.player;
            if (player == null || SpyglassAstronomyClient.isntHoldingSpyglass()) {
                Star.deselect();
            }            
        }

        if (OrbitingBody.selected != null) {
            LocalPlayer player = SpyglassAstronomyClient.client.player;
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

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            constellation.setVertices(bufferBuilder, false);
        }

        MeshData builtBuffer = bufferBuilder.buildOrThrow();
        if (constellationsBuffer != null) {
            constellationsBuffer.close();
        }
        constellationsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Constellations Buffer", 40, builtBuffer.vertexBuffer());
        constellationsCount = builtBuffer.drawState().indexCount();
        builtBuffer.close();
    }

    private void updateStars(int ticks) {
        if (SpyglassAstronomyClient.stars.isEmpty()) {
            starsCount = 0;
            return;
        }

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Star star : SpyglassAstronomyClient.stars) {
            star.update(ticks);
            star.setVertices(bufferBuilder);
        }

        if (starsBuffer != null) {
            starsBuffer.close();
        }
        MeshData builtBuffer = bufferBuilder.buildOrThrow();
        starsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Stars Buffer", 40, builtBuffer.vertexBuffer());
        starsCount = builtBuffer.drawState().indexCount();
        builtBuffer.close();
    }

    private void updateOrbits(int ticks) {
        if (SpyglassAstronomyClient.orbitingBodies.isEmpty()) {
            orbitingBodiesVisible = false;
            return;
        }

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Long day = SpyglassAstronomyClient.getDay();
        float dayFraction = SpyglassAstronomyClient.getDayFraction();

        Vector3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
        Vector3f normalisedReferencePosition = new Vector3f(referencePosition);
        normalisedReferencePosition.normalize();

        for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
            orbitingBody.update(ticks, referencePosition, normalisedReferencePosition, day, dayFraction);
            orbitingBody.setVertices(bufferBuilder);
        }

        MeshData builtBuffer = bufferBuilder.buildOrThrow();
        if (planetsBuffer != null) {
            planetsBuffer.close();
        }
        planetsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Planets Buffer", 40, builtBuffer.vertexBuffer());
        planetsCount = builtBuffer.drawState().indexCount();
        builtBuffer.close();
    }

    private void updateDrawingConstellation() {
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        SpyglassAstronomyClient.drawingConstellation.setVertices(bufferBuilder, true);

        MeshData builtBuffer = bufferBuilder.buildOrThrow();
        if (drawingBuffer != null) {
            drawingBuffer.close();
        }
        drawingBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Drawing Buffer", 40, builtBuffer.vertexBuffer());
        drawingCount = builtBuffer.drawState().indexCount();
        builtBuffer.close();
    }

    public void render(PoseStack matrices, SkyRenderState skyRenderState) {
        starVisibility = starsAlwaysVisible ? 1 : skyRenderState.starBrightness;
        if (starVisibility > 0) {
            float colorScale = starVisibility+Math.min(heightScale, 0.5f);

            if (constellationsVisible && SpyglassAstronomyClient.isDrawingConstellation || drawingCount > 0) {
                updateDrawingConstellation();
            }

            GpuTextureView mainColor = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
            GpuTextureView mainDepth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();

            if (starsVisible || constellationsVisible) {
                matrices.pushPose();
                matrices.mulPose(Axis.YP.rotationDegrees(-90.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(SpyglassAstronomyClient.getStarAngle()));
                matrices.mulPose(Axis.YP.rotationDegrees(45f));

                matrix4fStack.pushMatrix();
                matrix4fStack.mul(matrices.last().pose());

                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(colorScale, colorScale, colorScale, starVisibility), new Vector3f(), new Matrix4f());
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
                matrices.popPose();
                matrix4fStack.popMatrix();
            }

            if (orbitingBodiesVisible && planetsCount > 0) {
                matrices.pushPose();
                matrices.mulPose(Axis.ZP.rotationDegrees(SpyglassAstronomyClient.getPositionInOrbit(360f) * (1 - 1 / SpyglassAstronomyClient.earthOrbit.period) + 180));

                matrix4fStack.pushMatrix();
                matrix4fStack.mul(matrices.last().pose());

                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(matrix4fStack, new Vector4f(colorScale, colorScale, colorScale, starVisibility), new Vector3f(), new Matrix4f());
                RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Planets", mainColor, OptionalInt.empty(), mainDepth, OptionalDouble.empty());
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

                try {
                    renderPass.setPipeline(pipeline);
                    draw(renderPass, planetsBuffer, planetsCount);
                } catch (Throwable ignored) {}

                renderPass.close();
                matrices.popPose();
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
        heightScale = Mth.clamp((SpyglassAstronomyClient.getHeight()-32f)/256f, 0f, 1f);
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
        renderPass.setIndexBuffer(indexBuffer.getBuffer(count), indexBuffer.type());
        renderPass.drawIndexed(0,0, count, 1);
    }
}
