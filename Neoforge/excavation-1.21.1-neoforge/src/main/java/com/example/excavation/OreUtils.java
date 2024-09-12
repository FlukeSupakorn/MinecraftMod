package com.example.excavation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class OreUtils {
    /**
     * Checks if a given block state corresponds to an ore block.
     *
     * @param state The block state to check.
     * @return True if the block is an ore, false otherwise.
     */
    public static boolean isOre(BlockState state) {
        // Get the registry name (ResourceLocation) of the block
        ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        // Return true if the block's registry path contains "_ore", indicating it's an ore
        return registryName != null && registryName.getPath().contains("_ore");
    }
}
