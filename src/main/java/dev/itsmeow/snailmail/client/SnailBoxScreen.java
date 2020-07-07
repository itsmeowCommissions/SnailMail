package dev.itsmeow.snailmail.client;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SnailBoxScreen extends ContainerScreen<SnailBoxContainer> {
    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("snailmail:textures/gui/snail_box.png");

    public SnailBoxScreen(SnailBoxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 176;
        this.ySize = 178;
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.renderBackground();
        super.render(x, y, partialTicks);
        this.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int xStart = (this.width - this.xSize) / 2;
        int yStart = (this.height - this.ySize) / 2;
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        this.blit(xStart, yStart, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getFormattedText(), 8, 11, 0x404040);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, 84, 0x404040);
    }

}