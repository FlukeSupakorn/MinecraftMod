package com.example.excavation;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExcavationMod.MODID, value = Dist.CLIENT)
public class ExcavationParticleHandler {

    private static final int PARTICLE_INTERVAL = 20; // Adjust this value for particle spawn frequency
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && KeyBindings.oreMiningEnabled) {
            tickCounter++;
            if (tickCounter >= PARTICLE_INTERVAL) {
                tickCounter = 0;
                Minecraft minecraft = Minecraft.getInstance();
                BlockPos blockPos = getBlockLookingAt(minecraft);

                if (blockPos != null) {
                    BlockState blockState = minecraft.level.getBlockState(blockPos);

                    if (OreUtils.isOre(blockState)) {
                        Set<BlockPos> blocksToBreak = getBlocksToBreak(minecraft.level, blockPos, blockState, new HashSet<>(), 0);

                        for (BlockPos pos : blocksToBreak) {
                            spawnParticlesAroundBlock(pos);
                        }
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

    private static Set<BlockPos> getBlocksToBreak(ClientLevel level, BlockPos pos, BlockState state, Set<BlockPos> visited, int depth) {
        if (depth > Config.oreMiningRange) return visited;

        visited.add(pos);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Skip the current block itself
                    BlockPos adjacentPos = pos.offset(dx, dy, dz);
                    BlockState adjacentState = level.getBlockState(adjacentPos);

                    if (!visited.contains(adjacentPos) && adjacentState.getBlock() == state.getBlock()) {
                        getBlocksToBreak(level, adjacentPos, adjacentState, visited, depth + 1);
                    }
                }
            }
        }

        return visited;
    }

    private static void spawnParticlesAroundBlock(BlockPos pos) {
        ClientLevel level = Minecraft.getInstance().level;

        // Top face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        // Bottom face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() - 0.1, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        // North face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() - 0.1, 0.0, 0.0, 0.0);
        // South face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1.1, 0.0, 0.0, 0.0);
        // West face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() - 0.1, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        // East face
        level.addParticle(ParticleTypes.END_ROD, pos.getX() + 1.1, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }
}
