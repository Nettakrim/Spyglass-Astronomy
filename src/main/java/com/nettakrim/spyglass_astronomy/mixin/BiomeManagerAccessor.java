package com.nettakrim.spyglass_astronomy.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//https://github.com/Johni0702/bobby/blob/master/src/main/java/de/johni0702/minecraft/bobby/mixin/BiomeAccessAccessor.java
@Mixin(BiomeManager.class)
public interface BiomeManagerAccessor {
    @Accessor
    long getBiomeZoomSeed();
}
