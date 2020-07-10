package dev.itsmeow.snailmail.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SnailBoxMemberPopupScreen extends Screen {
    private static final ITextComponent TITLE = new TranslationTextComponent("container.snailmail.snail_box");
    public static final ResourceLocation MODAL_TEXTURE = new ResourceLocation("snailmail:textures/gui/modal.png");

    private SnailBoxMemberScreen parent;

    public SnailBoxMemberPopupScreen(SnailBoxMemberScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button((this.width - 200) / 2, this.height / 2 + 15, 200, 20, I18n.format("modal.snailmail.close"), btn -> {
            this.minecraft.displayGuiScreen(parent);
        }));
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.renderBackground();
        int modalXSize = 256;
        int modalYSize = 88;
        int modalXStart = (this.width - modalXSize) / 2;
        int modalYStart = (this.height - modalYSize) / 2;
        this.minecraft.getTextureManager().bindTexture(MODAL_TEXTURE);
        this.blit(modalXStart, modalYStart, 0, 0, modalXSize, modalYSize);
        String[] text = this.font.wrapFormattedStringToWidth(I18n.format("modal.snailmail.failed_to_add"), 240).split("\n");
        for(int i = 0; i < text.length; i++) {
            this.drawCenteredString(this.font, text[i], modalXStart + (modalXSize / 2), modalYStart + (modalYSize / 2) - (this.font.FONT_HEIGHT * (text.length - i)), 0xFFFFFF);
        }
        super.render(x, y, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.displayGuiScreen(parent);
    }

}