package com.example.itemstacker;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ItemStackerMod.MODID)
public class ItemStackHandler {

    private static final Set<ItemEntity> trackedItems = new HashSet<>();
    private static final int STACKING_RADIUS = 3; // Radius within which items should be checked
    private static Field onGroundField;

    static {
        try {
            onGroundField = Entity.class.getDeclaredField("onGround");
            onGroundField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity) {
            trackedItems.add((ItemEntity) entity);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        trackedItems.removeIf(itemEntity -> {
            if (isOnGround(itemEntity)) {
                setItemDisplayName(itemEntity);
                boolean hasSameType = checkAndCombineNearbyItems(itemEntity);
                sendNearbyItemsMessage(itemEntity, hasSameType);
                return true; // Remove from the set after processing
            }
            return false;
        });
    }

    private static boolean isOnGround(ItemEntity itemEntity) {
        try {
            return onGroundField.getBoolean(itemEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void setItemDisplayName(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        String itemName = stack.getHoverName().getString();
        int count = stack.getCount();
        MutableComponent customName = Component.literal(itemName + " x" + count)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))); // White color for the name
        itemEntity.setCustomName(customName);
        itemEntity.setCustomNameVisible(true);
    }

    private static boolean checkAndCombineNearbyItems(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        List<ItemEntity> nearbyItems = itemEntity.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, new AABB(
                itemEntity.getX() - STACKING_RADIUS, itemEntity.getY() - STACKING_RADIUS, itemEntity.getZ() - STACKING_RADIUS,
                itemEntity.getX() + STACKING_RADIUS, itemEntity.getY() + STACKING_RADIUS, itemEntity.getZ() + STACKING_RADIUS
        ));

        boolean hasSameType = false;
        for (ItemEntity nearbyItem : nearbyItems) {
            if (nearbyItem != itemEntity && ItemStack.isSameItemSameTags(nearbyItem.getItem(), stack)) {
                hasSameType = true;
                combineStacks(itemEntity, nearbyItem); // Combine into new item entity and discard old one
                nearbyItem.discard(); // Correctly remove the old item entity
                break;
            }
        }

        return hasSameType;
    }

    private static void combineStacks(ItemEntity targetEntity, ItemEntity sourceEntity) {
        ItemStack targetStack = targetEntity.getItem();
        ItemStack sourceStack = sourceEntity.getItem();
        int combinedCount = targetStack.getCount() + sourceStack.getCount();

        targetStack.setCount(combinedCount);
        sourceStack.setCount(0); // Clear the source stack

        setItemDisplayName(targetEntity);
    }

    private static void sendNearbyItemsMessage(ItemEntity itemEntity, boolean hasSameType) {
        if (itemEntity.getCommandSenderWorld().isClientSide) {
            return;
        }

        String itemName = itemEntity.getItem().getHoverName().getString();
        String messageText = hasSameType ? "has nearby items of the same type: " + itemName : "does not have nearby items of the same type.";
        Component message = Component.literal(itemEntity.getDisplayName().getString() + " " + messageText)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00))); // Yellow color for the message

        List<ServerPlayer> players = itemEntity.getCommandSenderWorld().getEntitiesOfClass(ServerPlayer.class, itemEntity.getBoundingBox().inflate(100));
        for (ServerPlayer player : players) {
            player.sendSystemMessage(message);
        }
    }
}
