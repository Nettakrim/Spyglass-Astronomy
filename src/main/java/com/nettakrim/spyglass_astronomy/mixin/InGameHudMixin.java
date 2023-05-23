package com.nettakrim.spyglass_astronomy.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    private static final Identifier STAR_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");    

    @Inject(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIFFIIII)V"))
    public void swapTexture(CallbackInfo ci){
        if (SpyglassAstronomyClient.editMode != 0) {
            RenderSystem.setShaderTexture(0, SpyglassAstronomyClient.editMode == 1 ? CONSTELLATION_SPYGLASS_SCOPE : STAR_SPYGLASS_SCOPE);
        }
    }
}
