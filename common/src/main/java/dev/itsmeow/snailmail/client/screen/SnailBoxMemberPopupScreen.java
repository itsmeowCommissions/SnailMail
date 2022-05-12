package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class SnailBoxMemberPopupScreen extends Screen {
    private static final Component TITLE = new TranslatableComponent("container.snailmail.snail_box");
    public static final ResourceLocation MODAL_TEXTURE = new ResourceLocation("snailmail:textures/gui/modal.png");

    private SnailBoxMemberScreen parent;

    public SnailBoxMemberPopupScreen(SnailBoxMemberScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addButton(new Button((this.width - 200) / 2, this.height / 2 + 15, 200, 20, new TranslatableComponent("modal.snailmail.close"), btn -> {
            Minecraft.getInstance().setScreen(parent);
        }));
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        this.renderBackground(stack);
        int modalXSize = 256;
        int modalYSize = 88;
        int modalXStart = (this.width - modalXSize) / 2;
        int modalYStart = (this.height - modalYSize) / 2;
        Minecraft.getInstance().getTextureManager().bind(MODAL_TEXTURE);
        this.blit(stack, modalXStart, modalYStart, 0, 0, modalXSize, modalYSize);
        List<FormattedCharSequence> text = this.font.split(new TranslatableComponent("modal.snailmail.failed_to_add"), 240);
        for(int i = 0; i < text.size(); i++) {
            this.font.draw(stack, text.get(i), modalXStart + (modalXSize / 2) - (this.font.width(text.get(i)) / 2), modalYStart + (modalYSize / 2) - (this.font.lineHeight * (text.size() - i)), 0xFFFFFF);
        }
        super.render(stack, x, y, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

}