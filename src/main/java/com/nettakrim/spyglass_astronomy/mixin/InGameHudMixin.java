package com.nettakrim.spyglass_astronomy.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = Identifier.of(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    @Unique
    private static final Identifier STAR_SPYGLASS_SCOPE = Identifier.of(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");

    @Inject(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;fill(Lnet/minecraft/client/render/RenderLayer;IIIIII)V",ordinal = 0))
    public void renderSpyglassMode(DrawContext context, float scale, CallbackInfo ci, @Local(name = "k") int k, @Local(name = "l") int l, @Local(name = "i") int i, @Local(name = "j") int j){
        if (SpyglassAstronomyClient.editMode != 0) {
            context.drawTexture(RenderLayer::getGuiTextured, SpyglassAstronomyClient.editMode == 1 ? CONSTELLATION_SPYGLASS_SCOPE : STAR_SPYGLASS_SCOPE, k, l, 0.0F, 0.0F, i, j, i, j);
        }
    }
}
