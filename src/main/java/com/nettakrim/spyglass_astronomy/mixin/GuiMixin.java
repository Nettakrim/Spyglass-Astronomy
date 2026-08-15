package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = Identifier.fromNamespaceAndPath(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    @Unique
    private static final Identifier STAR_SPYGLASS_SCOPE = Identifier.fromNamespaceAndPath(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");

    @Inject(method = "extractSpyglassOverlay",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;fill(Lcom/mojang/blaze3d/pipeline/RenderPipeline;IIIII)V", ordinal = 0))
    public void renderSpyglassMode(GuiGraphicsExtractor graphics, float scale, CallbackInfo ci, @Local(name = "left") int left, @Local(name = "top") int top, @Local(name = "width") int width, @Local(name = "height") int height){
        if (SpyglassAstronomyClient.editMode != 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SpyglassAstronomyClient.editMode == 1 ? CONSTELLATION_SPYGLASS_SCOPE : STAR_SPYGLASS_SCOPE, left, top, 0.0F, 0.0F, width, height, width, height);
        }
    }
}
