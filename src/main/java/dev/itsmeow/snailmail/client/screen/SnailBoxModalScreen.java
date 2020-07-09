package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.network.SendEnvelopePacket;
import dev.itsmeow.snailmail.network.SendEnvelopePacket.Type;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
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
        if(type != Type.TO_SERVER) {
            String arg = "";
            if(type == Type.INVALID_ADDRESS || type == Type.NO_BOXES) {
                ItemStack stack = parent.getContainer().getSlot(27).getStack();
                if(!stack.isEmpty() && stack.getItem() == ModItems.ENVELOPE_OPEN) {
                    arg = EnvelopeItem.getString(stack, "AddressedTo");
                }
            }
            String[] text = this.font.wrapFormattedStringToWidth(I18n.format("modal.snailmail." + type.name().toLowerCase(), arg), 240).split("\n");
            for(int i = 0; i < text.length; i++) {
                this.drawCenteredString(this.font, text[i], modalXStart + (modalXSize / 2), modalYStart + (modalYSize / 2) - (this.font.FONT_HEIGHT * (text.length - i)), 0xFFFFFF);
            }
            super.render(x, y, partialTicks);
        } else {
            int dotAmount = (int) ((System.currentTimeMillis() / 333L) % 4L);
            String dots = "";
            for(int i = 0; i < dotAmount; i++) {
                dots += ".";
            }
            this.drawCenteredString(this.font, I18n.format("modal.snailmail.sending") + dots, modalXStart + (modalXSize / 2), modalYStart + (modalYSize / 2), 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.displayGuiScreen(parent);
    }

    @Override
    public void receivePacket(SendEnvelopePacket packet) {
        if(packet.type != Type.SELECT_BOX) {
            this.type = packet.type;
        } else {
            this.minecraft.displayGuiScreen(parent);
            parent.receivePacket(packet);
        }
    }

}