package com.nettakrim.spyglass_astronomy.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SpyglassItem;
import net.minecraft.util.TypedActionResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;

@Mixin(SpyglassItem.class)
public class SpyglassItemMixin {
    @Inject(at = @At("TAIL"), method = "use")
    private void use(CallbackInfoReturnable<TypedActionResult<ItemStack>> ci) {
        SpyglassAstronomyClient.startUsingSpyglass();
    }
}
