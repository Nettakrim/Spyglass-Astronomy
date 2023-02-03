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
    private static final Identifier CONSTELLATION_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/constellation_spyglass_scope.png");
    private static final Identifier STAR_SPYGLASS_SCOPE = new Identifier(SpyglassAstronomyClient.MODID,"textures/star_spyglass_scope.png");    

    @Redirect(method = "renderSpyglassOverlay",at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    public void swapTexture(int i, Identifier identifier){
        switch (SpyglassAstronomyClient.editMode) {
            case 1:
                RenderSystem.setShaderTexture(i, CONSTELLATION_SPYGLASS_SCOPE);
                break;
            case 2:
                RenderSystem.setShaderTexture(i, STAR_SPYGLASS_SCOPE);
                break;
            default:
            RenderSystem.setShaderTexture(i, identifier);
        }
    }
}
