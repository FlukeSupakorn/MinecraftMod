package com.example.bigchest;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = BigChestMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping TOGGLE_HIGHLIGHT = new KeyMapping(
        "key.bigchest.toggle_highlight",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        "key.categories.bigchest"
    );

    public static boolean highlightEnabled = false;

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::onClientSetup);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        net.minecraft.client.Minecraft.getInstance().options.keyMappings =
                addKeyMapping(net.minecraft.client.Minecraft.getInstance().options.keyMappings, TOGGLE_HIGHLIGHT);
    }

    private static KeyMapping[] addKeyMapping(KeyMapping[] original, KeyMapping keyMapping) {
        KeyMapping[] newMappings = new KeyMapping[original.length + 1];
        System.arraycopy(original, 0, newMappings, 0, original.length);
        newMappings[original.length] = keyMapping;
        return newMappings;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (TOGGLE_HIGHLIGHT.consumeClick()) {
                highlightEnabled = !highlightEnabled;
                String statusMessage = "Big Chest Highlight is now " + (highlightEnabled ? "enabled" : "disabled");
                sendMessageToPlayer(statusMessage, "white");
            }
        }
    }

    private static void sendMessageToPlayer(String message, String color) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            int colorCode;
            switch (color.toLowerCase()) {
                case "red":
                    colorCode = 0xFF5555;
                    break;
                case "green":
                    colorCode = 0x55FF55;
                    break;
                case "yellow":
                    colorCode = 0xFFFF00;
                    break;
                default:
                    colorCode = 0xFFFFFF;
            }
            Component chatMessage = Component.literal(message).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorCode)));
            mc.player.sendSystemMessage(chatMessage);
        }
    }
}
