package com.example.excavation;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ExcavationHudRenderer {

    private static long lastToggleTime = 0;  // Keeps track of the last toggle time

    // This method will update the time when the key is toggled
    public static void updateLastToggleTime() {
        lastToggleTime = System.currentTimeMillis();
    }

    public static void renderHud() {
        Minecraft minecraft = Minecraft.getInstance();

        // Render the HUD without conditions (to make it always visible for testing)
        Font fontRenderer = minecraft.font;
        boolean isEnabled = KeyBindings.oreMiningEnabled;
        String statusText = "Ore Mining: " + (isEnabled ? "Enabled" : "Disabled");
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        int x = screenWidth - fontRenderer.width(statusText) - 10;
        int y = screenHeight - 20;
        int color = isEnabled ? 0x55FF55 : 0xFF5555; // Green if enabled, red if disabled

        GuiGraphics guiGraphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
        guiGraphics.drawString(fontRenderer, statusText, x, y, color, true);
        RenderSystem.disableBlend();
    }
}
