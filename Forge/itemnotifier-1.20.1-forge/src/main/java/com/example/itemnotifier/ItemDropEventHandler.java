package com.example.itemnotifier;

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

@Mod.EventBusSubscriber(modid = ItemNotifierMod.MODID)
public class ItemDropEventHandler {

    private static final Set<ItemEntity> trackedItems = new HashSet<>();
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
                sendItemDropMessage(itemEntity);
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
        MutableComponent customName = Component.literal(itemName + " x" + count).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00))); // Yellow color
        itemEntity.setCustomName(customName);
        itemEntity.setCustomNameVisible(true);
    }

    private static void sendItemDropMessage(ItemEntity itemEntity) {
        if (itemEntity.getCommandSenderWorld().isClientSide) {
            return;
        }

        List<ServerPlayer> players = itemEntity.getCommandSenderWorld().getEntitiesOfClass(ServerPlayer.class, itemEntity.getBoundingBox().inflate(100));

        String itemName = itemEntity.getItem().getHoverName().getString();
        int count = itemEntity.getItem().getCount();

        Component message = Component.literal(itemEntity.getDisplayName().getString() + " has dropped item: ")
                .append(Component.literal(itemName + " x" + count).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00)))); // Yellow color

        for (ServerPlayer player : players) {
            player.sendSystemMessage(message);
        }
    }
}
