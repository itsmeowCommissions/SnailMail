package dev.itsmeow.snailmail.client.screen;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.network.SendEnvelopePacket;
import dev.itsmeow.snailmail.network.SendEnvelopePacket.Type;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SnailBoxModalScreen extends Screen implements IEnvelopePacketReceiver {
    private static final ITextComponent TITLE = new TranslationTextComponent("container.snailmail.snail_box");
    public static final ResourceLocation MODAL_TEXTURE = new ResourceLocation("snailmail:textures/gui/modal.png");

    private Type type;
    private SnailBoxScreen parent;

    public SnailBoxModalScreen(SnailBoxScreen parent, Type type) {
        super(TITLE);
        this.parent = parent;
        this.type = type;
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
        this.drawTexture(stack, modalXStart, modalYStart, 0, 0, modalXSize, modalYSize);
        if(type != Type.TO_SERVER && type != Type.WAIT) {
            String arg = "";
            if(type == Type.INVALID_ADDRESS || type == Type.NO_BOXES) {
                ItemStack istack = parent.getContainer().getSlot(27).getStack();
                if(!istack.isEmpty() && istack.getItem() == ModItems.ENVELOPE_OPEN) {
                    arg = EnvelopeItem.getString(istack, "AddressedTo");
                }
            }
            List<IReorderingProcessor> text = this.textRenderer.wrapLines(new TranslationTextComponent("modal.snailmail." + type.name().toLowerCase(), arg), 240);
            for(int i = 0; i < text.size(); i++) {
                this.textRenderer.draw(stack, text.get(i), modalXStart + (modalXSize / 2) - (this.textRenderer.getWidth(text.get(i)) / 2), modalYStart + (modalYSize / 2) - (this.textRenderer.FONT_HEIGHT * (text.size() - i)), 0xFFFFFF);
            }
            super.render(stack, x, y, partialTicks);
        } else {
            int dotAmount = (int) ((System.currentTimeMillis() / 333L) % 4L);
            String dots = "";
            for(int i = 0; i < dotAmount; i++) {
                dots += ".";
            }
            AbstractGui.drawCenteredString(stack, this.textRenderer, new TranslationTextComponent("modal.snailmail.sending") + dots, modalXStart + (modalXSize / 2), modalYStart + (modalYSize / 2), 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        this.getMinecraft().displayGuiScreen(parent);
    }

    @Override
    public void receivePacket(SendEnvelopePacket packet) {
        if(packet.type != Type.SELECT_BOX) {
            this.type = packet.type;
        } else {
            this.getMinecraft().displayGuiScreen(parent);
            parent.receivePacket(packet);
        }
    }

}