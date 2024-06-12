package com.example.bigchest;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BigChestScreen extends AbstractContainerScreen<BigChestContainer> {

    public BigChestScreen(BigChestContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        // Clear background rendering code since we're not using a texture
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Font font = this.minecraft.font;
        guiGraphics.drawString(font, this.title.getString(), (this.imageWidth - font.width(this.title.getString())) / 2, 6, 4210752, false);
        guiGraphics.drawString(font, this.playerInventoryTitle.getString(), 8, this.imageHeight - 94, 4210752, false);
    }
}
