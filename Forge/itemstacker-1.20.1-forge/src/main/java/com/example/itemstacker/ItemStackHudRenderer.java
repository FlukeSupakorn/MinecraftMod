package com.example.itemstacker;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemStackerMod.MODID, value = Dist.CLIENT)
public class ItemStackHudRenderer {

    private static long lastToggleTime = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().toString().equals("minecraft:player_list")) {
            return; // Skip drawing if it's the player list overlay
        }

        if (System.currentTimeMillis() - lastToggleTime > 1000) {
            return; // Only display for 1 second after toggle
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        String statusText = "Stacking Mode: " + KeyBindings.getModeName(KeyBindings.stackingMode);
        int color = KeyBindings.stackingMode == 0 ? 0xFF5555 : 0x55FF55; // Red if off, green if on

        // Prepare to render text
        RenderSystem.enableBlend();

        // Set position to bottom right
        int x = screenWidth - fontRenderer.width(statusText) - 10;
        int y = screenHeight - 20;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.drawString(fontRenderer, statusText, x, y, color, true);

        RenderSystem.disableBlend();
    }

    public static void updateLastToggleTime() {
        lastToggleTime = System.currentTimeMillis();
    }
}
