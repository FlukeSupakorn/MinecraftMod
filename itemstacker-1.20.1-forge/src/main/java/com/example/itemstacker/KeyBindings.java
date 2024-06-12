package com.example.itemstacker;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = ItemStackerMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping TOGGLE_STACKING_MODE = new KeyMapping(
        "key.itemstacker.toggle_stacking_mode",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        "key.categories.itemstacker"
    );

    public static int stackingMode = 0;

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::onClientSetup);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        net.minecraft.client.Minecraft.getInstance().options.keyMappings =
                addKeyMapping(net.minecraft.client.Minecraft.getInstance().options.keyMappings, TOGGLE_STACKING_MODE);
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
            if (TOGGLE_STACKING_MODE.consumeClick()) {
                stackingMode = (stackingMode + 1) % 3;
                String statusMessage = "Stacking mode is now " + getModeName(stackingMode);
                sendMessageToPlayer(statusMessage, "white");
            }
        }
    }

    private static String getModeName(int mode) {
        switch (mode) {
            case 0:
                return "OFF";
            case 1:
                return "MERGE AT JOIN WORLD";
            case 2:
                return "MERGE ON GROUND";
            default:
                return "UNKNOWN";
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
