package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.world.ClientWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect", at = @At("RETURN"))
    private void saveSpace(CallbackInfo ci) {
        SpyglassAstronomyClient.saveSpace();
        SpyglassAstronomyClient.ready = false;
    }

    @Inject(method = "joinWorld", at = @At("TAIL"))
    private void loadSpace(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        SpyglassAstronomyClient.loadSpace(world, true);
    }
}
