package com.example.bigchest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BigChestMod.MODID)
public class BigChestEventHandler {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            BlockPos pos = event.getPos();
            BlockState state = event.getLevel().getBlockState(pos);

            if (state.getBlock() == Blocks.COBBLESTONE) {
                if (!event.getLevel().isClientSide) {
                    ServerPlayer player = (ServerPlayer) event.getEntity();
                    BigChestItemStackHandler chestInventory = new BigChestItemStackHandler(27);
                    player.openMenu(new SimpleMenuProvider((id, inventory, playerEntity) ->
                            new BigChestContainer(id, inventory, chestInventory), Component.literal("Big Chest")));
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
