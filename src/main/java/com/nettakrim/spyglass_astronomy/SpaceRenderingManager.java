package com.nettakrim.spyglass_astronomy;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.MeshData;
import net.hollowed.cosmos.Cosmos;
import net.hollowed.cosmos.config.CosmosConfig;
import net.hollowed.cosmos.renderer.CosmosStarRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.player.LocalPlayer;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Scanner;

import static net.minecraft.client.renderer.RenderPipelines.GLOBALS_SNIPPET;

public class SpaceRenderingManager {
    private int ticks = 0;
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

    private static final RenderPipeline pipeline = RenderPipelines.register(RenderPipeline.builder(GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withLocation(Identifier.fromNamespaceAndPath(SpyglassAstronomyClient.MODID, "pipeline/sga_objects"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .build());

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

        indexBuffer = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
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

    public void close() {
        closeBuffer(starsBuffer);
        closeBuffer(constellationsBuffer);
        closeBuffer(drawingBuffer);
        closeBuffer(planetsBuffer);
    }

    private void closeBuffer(GpuBuffer gpuBuffer) {
        if (gpuBuffer != null) {
            gpuBuffer.close();
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

    public void updateSpace() {
        ticks++;

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

        int size = 0;
        for (Constellation constellation : SpyglassAstronomyClient.constellations) {
            size += constellation.getLines().size();
        }

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * size * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (Constellation constellation : SpyglassAstronomyClient.constellations) {
                constellation.setVertices(bufferBuilder, false);
            }

            if (constellationsBuffer != null) {
                constellationsBuffer.close();
            }
            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                constellationsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Constellations Buffer", 40, mesh.vertexBuffer());
                constellationsCount = mesh.drawState().indexCount();
            }
        }
    }

    private void updateStars(int ticks) {
        if (SpyglassAstronomyClient.stars.isEmpty()) {
            starsCount = 0;
            return;
        }

        int size = SpyglassAstronomyClient.stars.size();

        VertexFormat format = SpyglassAstronomyClient.cosmosIsActive ? DefaultVertexFormat.POSITION_TEX_COLOR : DefaultVertexFormat.POSITION_COLOR;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(format.getVertexSize() * size * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, format);

            TextureAtlasSprite sprite;
            if (SpyglassAstronomyClient.cosmosIsActive) {
                TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
                sprite = atlas.getSprite(Cosmos.id("star"));
            } else {
                sprite = null;
            }

            for (Star star : SpyglassAstronomyClient.stars) {
                star.update(ticks);
                star.setVertices(bufferBuilder, sprite);
            }

            closeBuffer(starsBuffer);
            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                starsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Stars Buffer", 40, mesh.vertexBuffer());
                starsCount = mesh.drawState().indexCount();
            }
        }
    }

    private void updateOrbits(int ticks) {
        if (SpyglassAstronomyClient.orbitingBodies.isEmpty()) {
            orbitingBodiesVisible = false;
            return;
        }

        int size = SpyglassAstronomyClient.orbitingBodies.size() * 2;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * size * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            Long day = SpyglassAstronomyClient.getDay();
            float dayFraction = SpyglassAstronomyClient.getDayFraction();

            Vector3f referencePosition = SpyglassAstronomyClient.earthOrbit.getRotatedPositionAtGlobalTime(day, dayFraction, true);
            Vector3f normalisedReferencePosition = new Vector3f(referencePosition);
            normalisedReferencePosition.normalize();

            for (OrbitingBody orbitingBody : SpyglassAstronomyClient.orbitingBodies) {
                orbitingBody.update(ticks, referencePosition, normalisedReferencePosition, day, dayFraction);
                orbitingBody.setVertices(bufferBuilder);
            }

            closeBuffer(planetsBuffer);
            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                planetsBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Planets Buffer", 40, mesh.vertexBuffer());
                planetsCount = mesh.drawState().indexCount();
            }
        }
    }

    private void updateDrawingConstellation() {
        int size = 1;

        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * size * 4)) {
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);

            SpyglassAstronomyClient.drawingConstellation.setVertices(bufferBuilder, true);

            closeBuffer(drawingBuffer);
            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                drawingBuffer = RenderSystem.getDevice().createBuffer(() -> "SGA Drawing Buffer", 40, mesh.vertexBuffer());
                drawingCount = mesh.drawState().indexCount();
            }
        }
    }

    public void render(PoseStack matrices, float starBrightness, TextureAtlas celestialsAtlas) {
        starVisibility = starsAlwaysVisible ? 1 : starBrightness;
        if (starVisibility > 0) {
            float colorScale = starVisibility+Math.min(heightScale, 0.5f);

            if (constellationsVisible && SpyglassAstronomyClient.isDrawingConstellation || drawingCount > 0) {
                updateDrawingConstellation();
            }

            RenderTarget renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            GpuTextureView mainColor = renderTarget.getColorTextureView();
            GpuTextureView mainDepth = renderTarget.getDepthTextureView();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

            assert mainColor != null;
            Vector4f defaultModulator = new Vector4f(colorScale, colorScale, colorScale, starVisibility);

            if (starsVisible || constellationsVisible) {
                matrices.pushPose();
                matrices.mulPose(Axis.YP.rotationDegrees(-90.0f));
                matrices.mulPose(Axis.XP.rotationDegrees(SpyglassAstronomyClient.getStarAngle()));
                matrices.mulPose(Axis.YP.rotationDegrees(45f));

                modelViewStack.pushMatrix();
                modelViewStack.mul(matrices.last().pose());

                if (starsVisible) {
                    Vector4f starModulator;
                    RenderPipeline starPipeline;
                    if (SpyglassAstronomyClient.cosmosIsActive) {
                        // colorScale will brighten the stars further when high up, so it acts similarly to the default CosmosConfig.brightnessMultiplier
                        // cosmos brightness may as well be used anyway, but such that the default value works for sgas intended gameplay (so, darker than vanilla cosmos when low down)
                        float boost = CosmosConfig.brightnessMultiplier / 1.75f;
                        assert Minecraft.getInstance().level != null;
                        float time = Minecraft.getInstance().level.getGameTime() % 24000 / 20.0F;
                        starModulator = new Vector4f(colorScale * boost, CosmosConfig.twinkleFrequency.getFirst().floatValue(), CosmosConfig.twinkleFrequency.get(1).floatValue(), time);
                        starPipeline = CosmosStarRendering.COSMOS_STARS;
                    } else {
                        starModulator = defaultModulator;
                        starPipeline = pipeline;
                    }

                    GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack), starModulator);
                    try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", mainColor, Optional.empty(), mainDepth, OptionalDouble.empty())) {
                        renderPass.setPipeline(starPipeline);
                        RenderSystem.bindDefaultUniforms(renderPass);
                        renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                        if (SpyglassAstronomyClient.cosmosIsActive) {
                            renderPass.bindTexture("Sampler0", celestialsAtlas.getTextureView(), celestialsAtlas.getSampler());
                        }

                        draw(renderPass, starsBuffer, starsCount);
                    }
                }

                if (constellationsVisible) {
                    GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack), defaultModulator);
                    try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Constellations", mainColor, Optional.empty(), mainDepth, OptionalDouble.empty())) {
                        renderPass.setPipeline(pipeline);
                        RenderSystem.bindDefaultUniforms(renderPass);
                        renderPass.setUniform("DynamicTransforms", dynamicTransforms);

                        draw(renderPass, constellationsBuffer, constellationsCount);

                        if (SpyglassAstronomyClient.isDrawingConstellation) {
                            draw(renderPass, drawingBuffer, drawingCount);
                        }
                    }
                }
                matrices.popPose();
                modelViewStack.popMatrix();
            }

            if (orbitingBodiesVisible && planetsCount > 0) {
                matrices.pushPose();
                matrices.mulPose(Axis.ZP.rotationDegrees(SpyglassAstronomyClient.getPositionInOrbit(360f) * (1 - 1 / SpyglassAstronomyClient.earthOrbit.period) + 180));

                modelViewStack.pushMatrix();
                modelViewStack.mul(matrices.last().pose());

                GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(modelViewStack), defaultModulator);
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Planets", mainColor, Optional.empty(), mainDepth, OptionalDouble.empty())) {
                    renderPass.setPipeline(pipeline);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", gpuBufferSlice);

                    draw(renderPass, planetsBuffer, planetsCount);
                } catch (Throwable ignored) {}

                matrices.popPose();
                modelViewStack.popMatrix();
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

        renderPass.setVertexBuffer(0, gpuBuffer.slice());
        renderPass.setIndexBuffer(indexBuffer.getBuffer(count), indexBuffer.type());
        renderPass.drawIndexed(count, 1, 0, 0, 0);
    }
}
