package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = @At("TAIL"), method = "updateMouse")
    public void updateMouse(CallbackInfo ci) {
        if (SpyglassAstronomyClient.isDrawingConstellation) {
            SpyglassAstronomyClient.updateDrawingConstellation();
        }
    }
    
    @Inject(method = "onMouseScroll",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci){
        ClientPlayerEntity player = SpyglassAstronomyClient.client.player;
        if(player != null && player.isUsingSpyglass()){
            //scroll
            ci.cancel();
        }
    }
}
