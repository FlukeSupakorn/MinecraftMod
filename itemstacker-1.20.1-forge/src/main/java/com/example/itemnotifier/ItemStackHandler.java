package com.example.itemstacker;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ItemStackerMod.MODID)
public class ItemStackHandler {

    private static final Set<ItemEntity> trackedItems = new HashSet<>();
    private static final Map<ItemEntity, Long> lastChecked = new HashMap<>();
    private static final int STACKING_RADIUS = 5; // Radius within which items should be checked
    private static final long CHECK_INTERVAL = 1000; // Check interval in milliseconds
    private static final int PLAYER_RADIUS = 32; // Radius around the player to check items
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
        if (KeyBindings.stackingMode == 0) {
            return; // Stacking is off
        }

        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity) {
            trackedItems.add((ItemEntity) entity);
            lastChecked.put((ItemEntity) entity, System.currentTimeMillis());
            if (KeyBindings.stackingMode == 1) {
                // Merge at join world
                checkAndCombineNearbyItems((ItemEntity) entity);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event) {
        if (KeyBindings.stackingMode == 0) {
            return; // Stacking is off
        }

        ItemEntity itemEntity = event.getOriginalEntity();
        int pickupCount = event.getStack().getCount();
        updateItemCount(itemEntity, -pickupCount);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (KeyBindings.stackingMode == 0) {
            return; // Stacking is off
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            List<ItemEntity> nearbyItems = player.getCommandSenderWorld().getEntitiesOfClass(ItemEntity.class, new AABB(
                player.getX() - PLAYER_RADIUS, player.getY() - PLAYER_RADIUS, player.getZ() - PLAYER_RADIUS,
                player.getX() + PLAYER_RADIUS, player.getY() + PLAYER_RADIUS, player.getZ() + PLAYER_RADIUS
            ));

            for (ItemEntity itemEntity : nearbyItems) {
                if (itemEntity.isRemoved() || !itemEntity.isAlive()) {
                    trackedItems.remove(itemEntity);
                    lastChecked.remove(itemEntity);
                    continue;
                }

                if (currentTime - lastChecked.getOrDefault(itemEntity, 0L) < CHECK_INTERVAL) {
                    continue;
                }

                lastChecked.put(itemEntity, currentTime);

                if (KeyBindings.stackingMode == 1 || (KeyBindings.stackingMode == 2 && isOnGround(itemEntity))) {
                    if (isOnGround(itemEntity)) {
                        setItemDisplayName(itemEntity);
                        boolean hasSameType = checkAndCombineNearbyItems(itemEntity);
                        // sendNearbyItemsMessage(itemEntity, hasSameType); // Commented out debug message
                    } else {
                        int previousCount = getScoreboardCount(itemEntity);
                        int currentCount = itemEntity.getItem().getCount();
                        if (currentCount != previousCount) {
                            updateScoreboard(itemEntity, currentCount);
                            // sendCountChangeMessage(itemEntity, currentCount - previousCount); // Commented out debug message
                        }
                    }
                }
            }
        }
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
        MutableComponent customName = Component.literal(itemName)
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))) // White color for the name
                .append(Component.literal(" x" + count).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00)))); // Yellow color for the count
        itemEntity.setCustomName(customName);
        itemEntity.setCustomNameVisible(true);

        updateScoreboard(itemEntity, count);
    }

    private static void updateScoreboard(ItemEntity itemEntity, int count) {
        Scoreboard scoreboard = itemEntity.getCommandSenderWorld().getScoreboard();
        String itemName = itemEntity.getItem().getHoverName().getString();
        if (scoreboard.getObjective(itemName) == null) {
            scoreboard.addObjective(itemName, ObjectiveCriteria.DUMMY, Component.literal(itemName), ObjectiveCriteria.RenderType.INTEGER);
        }
        Score score = scoreboard.getOrCreatePlayerScore(itemName, scoreboard.getObjective(itemName));
        score.setScore(count);
    }

    private static int getScoreboardCount(ItemEntity itemEntity) {
        Scoreboard scoreboard = itemEntity.getCommandSenderWorld().getScoreboard();
        String itemName = itemEntity.getItem().getHoverName().getString();
        Score score = scoreboard.getOrCreatePlayerScore(itemName, scoreboard.getObjective(itemName));
        return score.getScore();
    }

    private static void updateItemCount(ItemEntity itemEntity, int delta) {
        ItemStack stack = itemEntity.getItem();
        int newCount = stack.getCount() + delta;

        if (newCount > 0) {
            stack.setCount(newCount);
            setItemDisplayName(itemEntity);
        } else {
            itemEntity.discard();
        }
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
                int oldCount = itemEntity.getItem().getCount();
                combineStacks(itemEntity, nearbyItem); // Combine into new item entity and discard old one
                nearbyItem.discard(); // Correctly remove the old item entity
                int newCount = itemEntity.getItem().getCount();
                // sendCountChangeMessage(itemEntity, newCount - oldCount); // Commented out debug message
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

    /*
    private static void sendCountChangeMessage(ItemEntity itemEntity, int change) {
        if (itemEntity.getCommandSenderWorld().isClientSide) {
            return;
        }

        String itemName = itemEntity.getItem().getHoverName().getString();
        String changeText = change > 0 ? "increased by " + change : "decreased by " + (-change);
        TextColor changeColor = change > 0 ? TextColor.fromRgb(0x00FF00) : TextColor.fromRgb(0xFF0000); // Green for increase, red for decrease
        Component message = Component.literal(itemName + " stack " + changeText)
                .withStyle(style -> style.withColor(changeColor));

        List<ServerPlayer> players = itemEntity.getCommandSenderWorld().getEntitiesOfClass(ServerPlayer.class, itemEntity.getBoundingBox().inflate(100));
        for (ServerPlayer player : players) {
            player.sendSystemMessage(message);
        }
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
    */
}
