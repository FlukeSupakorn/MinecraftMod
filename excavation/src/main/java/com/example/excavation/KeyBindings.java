package com.example.excavation;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
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
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExcavationMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping TOGGLE_ORE_MINING = new KeyMapping(
        "key.excavation.toggle_ore_mining",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        "key.categories.excavation"
    );

    public static boolean oreMiningEnabled = true;

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::onClientSetup);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        net.minecraft.client.Minecraft.getInstance().options.keyMappings =
                addKeyMapping(net.minecraft.client.Minecraft.getInstance().options.keyMappings, TOGGLE_ORE_MINING);
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
            if (TOGGLE_ORE_MINING.consumeClick()) {
                oreMiningEnabled = !oreMiningEnabled;
                String statusMessage = "Ore mining is now" + (oreMiningEnabled ? "enabled" : "disabled");
                sendMessageToPlayer(statusMessage, oreMiningEnabled ? "green" : "red");
            }
        }
    }

    private static void sendMessageToPlayer(String message, String color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            int colorCode;
            switch (color.toLowerCase()) {
                case "red":
                    colorCode = 0xFF5555;
                    break;
                case "green":
                    colorCode = 0x55FF55;
                    break;
                default:
                    colorCode = 0xFFFFFF;
            }
            Component chatMessage = Component.literal(message).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorCode)));
            mc.player.sendSystemMessage(chatMessage);
        }
    }
}
