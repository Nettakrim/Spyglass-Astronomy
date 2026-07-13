package com.nettakrim.spyglass_astronomy.mixin;

import com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient;
import net.hollowed.cosmos.config.CosmosConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CosmosConfig.class)
public class CosmosConfigMixin {
    @Inject(method = "writeChanges", at = @At("RETURN"))
    void regenerateStars(CallbackInfo ci) {
        // only update cosmos activeness when closing the ui, so that it doesnt change rendering behaviour before recreating the buffers
        SpyglassAstronomyClient.cosmosIsActive = CosmosConfig.enabled;
        // star sizes and colors are different depending on if cosmos is enabled, so they need to be remade
        SpyglassAstronomyClient.spaceDataManager.backupStars();
        SpyglassAstronomyClient.generateStars(null, true, false);
        SpyglassAstronomyClient.spaceRenderingManager.updateSpace();
    }
}
