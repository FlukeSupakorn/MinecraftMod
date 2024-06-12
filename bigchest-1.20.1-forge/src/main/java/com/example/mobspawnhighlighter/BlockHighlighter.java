package com.example.mobspawnhighlighter;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobSpawnHighlighterMod.MODID)
public class BlockHighlighter {

    private static final int RADIUS_XZ = 32;
    private static final int RADIUS_Y = 8;
    private static final int PARTICLE_INTERVAL = 40; // Spawns particles every second (20 ticks)

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!KeyBindings.highlightEnabled || !isHoldingTorch()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;
            if (tickCounter < PARTICLE_INTERVAL) {
                return;
            }
            tickCounter = 0;

            Level world = mc.level;
            BlockPos playerPos = mc.player.blockPosition();

            for (int x = -RADIUS_XZ; x <= RADIUS_XZ; x++) {
                for (int z = -RADIUS_XZ; z <= RADIUS_XZ; z++) {
                    for (int y = -RADIUS_Y; y <= RADIUS_Y; y++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        if (isSurfaceBlock(world, pos) && world.getMaxLocalRawBrightness(pos.above()) <= 7) {
                            world.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
        }
    }

    private static boolean isSurfaceBlock(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() != Blocks.AIR && world.getBlockState(pos.above()).getBlock() == Blocks.AIR;
    }

    private static boolean isHoldingTorch() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return mc.player.getMainHandItem().getItem() == Items.TORCH || mc.player.getOffhandItem().getItem() == Items.TORCH;
        }
        return false;
    }
}
