package com.example.mobspawnhighlighter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobSpawnHighlighterMod.MODID, value = Dist.CLIENT)
public class MobSpawnHighlighterHudRenderer {

    private static long lastToggleTime = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().toString().equals("minecraft:player_list")) {
            return; // Skip drawing if it's the player list overlay
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font fontRenderer = minecraft.font;

        long currentTime = System.currentTimeMillis();
        int shiftPressCount = KeyBindings.getShiftPressCount();
        StringBuilder statusTextBuilder = new StringBuilder();
        for (int i = 0; i < shiftPressCount; i++) {
            statusTextBuilder.append("∎ ");
        }
        String statusText = statusTextBuilder.toString().trim();

        if (KeyBindings.shouldShowStatusText()) {
            statusText = KeyBindings.getModeStatusText();
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Prepare to render text
        RenderSystem.enableBlend();

        // Set position to above the tool bar
        int x = screenWidth / 2 - fontRenderer.width(statusText) / 2;
        int y = screenHeight - 40;

        int color;
        if (KeyBindings.shouldShowStatusText()) {
            color = KeyBindings.getHighlightColor();
        } else {
            color = KeyBindings.getProgressColor(); // Green for enabling, red for disabling
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.drawString(fontRenderer, statusText, x, y, color, true);

        RenderSystem.disableBlend();

        if (KeyBindings.shouldShowStatusText() && currentTime - KeyBindings.getStatusTextDisplayTime() >= KeyBindings.STATUS_TEXT_DURATION) {
            KeyBindings.resetShowStatusText();
        }
    }

    public static void updateLastToggleTime() {
        lastToggleTime = System.currentTimeMillis();
    }
}
