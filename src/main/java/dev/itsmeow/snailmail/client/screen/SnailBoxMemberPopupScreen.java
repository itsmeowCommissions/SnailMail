package dev.itsmeow.snailmail.client.screen;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
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
        this.addButton(new Button((this.width - 200) / 2, this.height / 2 + 15, 200, 20, new TranslationTextComponent("modal.snailmail.close"), btn -> {
            this.getMinecraft().displayGuiScreen(parent);
        }));
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float partialTicks) {
        this.renderBackground(stack);
        int modalXSize = 256;
        int modalYSize = 88;
        int modalXStart = (this.width - modalXSize) / 2;
        int modalYStart = (this.height - modalYSize) / 2;
        this.getMinecraft().getTextureManager().bindTexture(MODAL_TEXTURE);
        this.blit(stack, modalXStart, modalYStart, 0, 0, modalXSize, modalYSize);
        List<IReorderingProcessor> text = this.font.trimStringToWidth(new TranslationTextComponent("modal.snailmail.failed_to_add"), 240);
        for(int i = 0; i < text.size(); i++) {
            this.font.func_238422_b_(stack, text.get(i), modalXStart + (modalXSize / 2) - (this.font.func_243245_a(text.get(i)) / 2), modalYStart + (modalYSize / 2) - (this.font.FONT_HEIGHT * (text.size() - i)), 0xFFFFFF);
        }
        super.render(stack, x, y, partialTicks);
    }

    @Override
    public void closeScreen() {
        this.getMinecraft().displayGuiScreen(parent);
    }

}