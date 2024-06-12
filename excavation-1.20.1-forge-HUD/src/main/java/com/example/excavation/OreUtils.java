package com.example.excavation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class OreUtils {
    public static boolean isOre(BlockState state) {
        ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return registryName != null && registryName.getPath().contains("_ore");
    }
}
