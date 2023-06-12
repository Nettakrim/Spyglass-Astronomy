package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    private static final Identifier STAR_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");    

    @Redirect(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V"))
    public void swapTexture(DrawContext instance, Identifier texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight){
        if (SpyglassAstronomyClient.editMode != 0) {
            texture =  SpyglassAstronomyClient.editMode == 1 ? CONSTELLATION_SPYGLASS_SCOPE : STAR_SPYGLASS_SCOPE;
        }
        instance.drawTexture(texture, x, y, z, u, v, width, height, textureWidth, textureHeight);
    }
}
