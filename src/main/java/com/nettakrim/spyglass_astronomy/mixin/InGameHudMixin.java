package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = Identifier.of(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    @Unique
    private static final Identifier STAR_SPYGLASS_SCOPE = Identifier.of(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");

    @Redirect(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"))
    public void swapTexture(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight){
        if (SpyglassAstronomyClient.editMode != 0) {
            sprite = SpyglassAstronomyClient.editMode == 1 ? CONSTELLATION_SPYGLASS_SCOPE : STAR_SPYGLASS_SCOPE;
        }
        instance.drawTexture(renderLayers, sprite, x, y, u, v, width, height, textureWidth, textureHeight);
    }
}
