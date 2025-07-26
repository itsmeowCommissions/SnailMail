package dev.itsmeow.snailmail.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class SnailBoxMemberPopupScreen extends Screen {
    private static final Component TITLE = Component.translatable("container.snailmail.snail_box");
    public static final ResourceLocation MODAL_TEXTURE = new ResourceLocation("snailmail:textures/gui/modal.png");

    private SnailBoxMemberScreen parent;

    public SnailBoxMemberPopupScreen(SnailBoxMemberScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.translatable("modal.snailmail.close"), btn -> {
            Minecraft.getInstance().setScreen(parent);
        }).pos((this.width - 200) / 2, this.height / 2 + 15).size(200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        this.renderBackground(guiGraphics);
        int modalXSize = 256;
        int modalYSize = 88;
        int modalXStart = (this.width - modalXSize) / 2;
        int modalYStart = (this.height - modalYSize) / 2;
        guiGraphics.blit(MODAL_TEXTURE, modalXStart, modalYStart, 0, 0, modalXSize, modalYSize);
        List<FormattedCharSequence> text = this.font.split(Component.translatable("modal.snailmail.failed_to_add"), 240);
        for(int i = 0; i < text.size(); i++) {
            guiGraphics.drawString(this.font, text.get(i), modalXStart + (modalXSize / 2) - (this.font.width(text.get(i)) / 2), modalYStart + (modalYSize / 2) - (this.font.lineHeight * (text.size() - i)), 0xFFFFFF, false);
        }
        super.render(guiGraphics, x, y, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

}