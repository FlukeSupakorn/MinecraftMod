package com.example.excavation;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExcavationMod.MODID, value = Dist.CLIENT)
public class ExcavationHudRenderer {

    private static long lastToggleTime = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().toString().equals("minecraft:player_list")) {
            return; // Skip drawing if it's the player list overlay
        }

        if (System.currentTimeMillis() - lastToggleTime > 1000) {
            return; // Only display for 0.5 seconds after toggle
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;

        boolean isEnabled = KeyBindings.oreMiningEnabled;
        String statusText = "Ore Mining: " + (isEnabled ? "Enabled" : "Disabled");
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Prepare to render text
        RenderSystem.enableBlend();

        // Set position to bottom right
        int x = screenWidth - fontRenderer.width(statusText) - 10;
        int y = screenHeight - 20;

        int color = isEnabled ? 0x55FF55 : 0xFF5555; // Green if enabled, red if disabled

        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.drawString(fontRenderer, statusText, x, y, color, true);

        RenderSystem.disableBlend();
    }

    public static void updateLastToggleTime() {
        lastToggleTime = System.currentTimeMillis();
    }
}
