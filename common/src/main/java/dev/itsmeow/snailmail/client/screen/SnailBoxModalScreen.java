package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.network.SendEnvelopePacket;
import dev.itsmeow.snailmail.network.SendEnvelopePacket.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SnailBoxModalScreen extends Screen implements IEnvelopePacketReceiver {
    private static final Component TITLE = Component.translatable("container.snailmail.snail_box");
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
        if(type != Type.TO_SERVER && type != Type.WAIT) {
            String arg = "";
            if(type == Type.INVALID_ADDRESS || type == Type.NO_BOXES) {
                ItemStack istack = parent.getMenu().getSlot(27).getItem();
                if(!istack.isEmpty() && istack.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                    arg = EnvelopeItem.getString(istack, "AddressedTo");
                }
            }
            List<FormattedCharSequence> text = this.font.split(Component.translatable("modal.snailmail." + type.name().toLowerCase(), arg), 240);
            for(int i = 0; i < text.size(); i++) {
                guiGraphics.drawString(this.font, text.get(i), modalXStart + (modalXSize / 2) - (this.font.width(text.get(i)) / 2), modalYStart + (modalYSize / 2) - (this.font.lineHeight * (text.size() - i)), 0xFFFFFF, false);
            }
            super.render(guiGraphics, x, y, partialTicks);
        } else {
            int dotAmount = (int) ((System.currentTimeMillis() / 333L) % 4L);
            String dots = "";
            for(int i = 0; i < dotAmount; i++) {
                dots += ".";
            }
            guiGraphics.drawCenteredString(this.font, Component.translatable("modal.snailmail.sending").append(Component.literal(dots)).getString(), modalXStart + (modalXSize / 2), modalYStart + (modalYSize / 2), 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void receivePacket(SendEnvelopePacket packet) {
        if(packet.type != Type.SELECT_BOX) {
            this.type = packet.type;
        } else {
            Minecraft.getInstance().setScreen(parent);
            parent.receivePacket(packet);
        }
    }

}