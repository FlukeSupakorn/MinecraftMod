package com.example.mobspawnhighlighter;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MobSpawnHighlighterMod.MODID, value = Dist.CLIENT)
public class KeyBindings {
    public static final KeyMapping TOGGLE_HIGHLIGHT = new KeyMapping(
        "key.mobspawnhighlighter.toggle_highlight",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        "key.categories.mobspawnhighlighter"
    );

    public static boolean highlightEnabled = false;
    private static long lastToggleTime = 0;

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyBindings::onClientSetup);
        MinecraftForge.EVENT_BUS.register(KeyBindings.class);
    }

    private static void onClientSetup(final FMLClientSetupEvent event) {
        Minecraft.getInstance().options.keyMappings =
                addKeyMapping(Minecraft.getInstance().options.keyMappings, TOGGLE_HIGHLIGHT);
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
                lastToggleTime = System.currentTimeMillis();
            }
        }
    }

    public static long getLastToggleTime() {
        return lastToggleTime;
    }

    public static String getHighlightStatus() {
        return highlightEnabled ? "enabled" : "disabled";
    }

    public static int getHighlightColor() {
        return highlightEnabled ? 0x55FF55 : 0xFF5555;
    }
}
