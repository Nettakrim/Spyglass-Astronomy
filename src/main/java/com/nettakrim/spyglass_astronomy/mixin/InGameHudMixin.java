package com.nettakrim.spyglass_astronomy.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final Identifier EDITING_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/editing_spyglass_scope.png");

    @Redirect(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    public void swapTexture(int i, Identifier identifier){
        if (SpyglassAstronomyClient.isInEditMode) {
            RenderSystem.setShaderTexture(i, EDITING_SPYGLASS_SCOPE);
        } else {
            RenderSystem.setShaderTexture(i, identifier);
        }
    }
}
