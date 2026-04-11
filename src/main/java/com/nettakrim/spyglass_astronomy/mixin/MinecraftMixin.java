package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("RETURN"))
    private void saveSpace(CallbackInfo ci) {
        SpyglassAstronomyClient.saveSpace();
        SpyglassAstronomyClient.ready = false;
    }

    @Inject(method = "setLevel", at = @At("TAIL"))
    private void loadSpace(ClientLevel level, CallbackInfo ci) {
        SpyglassAstronomyClient.loadSpace(level, true);
    }
}
