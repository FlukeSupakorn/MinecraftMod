package com.example.itemstacker;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemStackerMod.MODID)
public class ItemStackHandler {

    private static final int STACKING_RADIUS = 5; // Radius within which items should stack

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof ItemEntity)) {
            return;
        }

        ItemEntity newItemEntity = (ItemEntity) entity;
        if (!newItemEntity.isAlive() || newItemEntity.getItem().isEmpty()) {
            return;
        }

        checkAndStackItems(newItemEntity);
    }

    @SubscribeEvent
    public static void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event) {
        ItemEntity itemEntity = event.getOriginalEntity();
        ItemStack itemStack = itemEntity.getItem();

        int pickupCount = event.getStack().getCount();
        int entityCount = itemStack.getCount();

        if (entityCount > pickupCount) {
            itemStack.setCount(entityCount - pickupCount);
            event.getStack().setCount(pickupCount);
            event.setCanceled(true); // Prevent the original stack from being removed
            updateItemEntityName(itemEntity); // Update the display name after modifying the count
        } else {
            itemStack.setCount(0);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (ServerLevel world : event.getServer().getAllLevels()) {
            List<ItemEntity> itemEntities = world.getEntitiesOfClass(ItemEntity.class, new AABB(
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
            ));

            for (ItemEntity itemEntity : itemEntities) {
                if (itemEntity.isAlive() && !itemEntity.getItem().isEmpty()) {
                    checkAndStackItems(itemEntity);
                }
            }
        }
    }

    private static void checkAndStackItems(ItemEntity itemEntity) {
        List<ItemEntity> nearbyItems = itemEntity.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, new AABB(
                itemEntity.getX() - STACKING_RADIUS, itemEntity.getY() - STACKING_RADIUS, itemEntity.getZ() - STACKING_RADIUS,
                itemEntity.getX() + STACKING_RADIUS, itemEntity.getY() + STACKING_RADIUS, itemEntity.getZ() + STACKING_RADIUS
        ));

        for (ItemEntity existingItemEntity : nearbyItems) {
            if (existingItemEntity == itemEntity) {
                continue;
            }

            if (existingItemEntity.isAlive() && canStack(existingItemEntity.getItem(), itemEntity.getItem())) {
                combineStacks(itemEntity, existingItemEntity); // Combine into new item entity and discard old one
                existingItemEntity.discard(); // Correctly remove the old item entity
                return;
            }
        }
    }

    private static boolean canStack(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }

    private static void combineStacks(ItemEntity targetEntity, ItemEntity sourceEntity) {
        ItemStack targetStack = targetEntity.getItem();
        ItemStack sourceStack = sourceEntity.getItem();
        int combinedCount = targetStack.getCount() + sourceStack.getCount();

        targetStack.setCount(combinedCount);
        sourceStack.setCount(0); // Clear the source stack

        updateItemEntityName(targetEntity);
    }

    private static void updateItemEntityName(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.getCount() > 1) {
            String itemName = stack.getHoverName().getString();
            String countString = " x" + stack.getCount();
            MutableComponent customName = Component.literal(itemName).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))) // Green for items with count > 1
                    .append(Component.literal(countString).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00)))); // Red for count
            itemEntity.setCustomName(customName);
            itemEntity.setCustomNameVisible(true);
        } else {
            itemEntity.setCustomName(null);
            itemEntity.setCustomNameVisible(false);
        }
    }
}
