package com.example.excavation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;

public class ExcavationParticleHandler {

    private static final int PARTICLE_INTERVAL = 20; // Adjust this value for particle spawn frequency
    private static int tickCounter = 0;

    public static void onClientTick() {
        if (KeyBindings.oreMiningEnabled) {
            tickCounter++;
            if (tickCounter >= PARTICLE_INTERVAL) {
                tickCounter = 0;
                Minecraft minecraft = Minecraft.getInstance();
                BlockPos blockPos = getBlockLookingAt(minecraft);

                if (blockPos != null) {
                    BlockState blockState = minecraft.level.getBlockState(blockPos);

                    if (OreUtils.isOre(blockState)) {
                        spawnParticlesAroundBlock(blockPos);
                    }
                }
            }
        }
    }

    private static BlockPos getBlockLookingAt(Minecraft minecraft) {
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            return ((net.minecraft.world.phys.BlockHitResult) minecraft.hitResult).getBlockPos();
        }
        return null;
    }

    private static void spawnParticlesAroundBlock(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}
