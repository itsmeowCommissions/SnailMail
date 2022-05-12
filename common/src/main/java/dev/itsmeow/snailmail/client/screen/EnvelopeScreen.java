package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModNetwork;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.network.OpenSnailBoxGUIPacket;
import dev.itsmeow.snailmail.network.SetEnvelopeNamePacket;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnvelopeScreen extends AbstractContainerScreen<EnvelopeMenu> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("snailmail:textures/gui/envelope_open.png");
    private EditBox toField;
    private EditBox fromField;

    public EnvelopeScreen(EnvelopeMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.toField = new EditBox(this.font, i + 92, j + 10, 58, 10, new TranslatableComponent("container.snailmail.envelope.textfield.to")) {

            @Override
            public boolean charTyped(char c, int p_charTyped_2_) {
                if(!this.canConsumeInput()) {
                    return false;
                } else if(RandomUtil.isAllowedCharacter(c, false)) {
                    this.insertText(Character.toString(c));

                    return true;
                } else {
                    return false;
                }
            }

        };
        this.toField.setValue(this.menu.clientStartToName);
        this.toField.setCanLoseFocus(true);
        this.toField.setTextColor(0xFFFFFF);
        this.toField.setTextColorUneditable(0xFFFFFF);
        this.toField.setBordered(false);
        this.toField.setMaxLength(35);
        this.toField.setResponder(newText -> {
            ModNetwork.HANDLER.sendToServer(new SetEnvelopeNamePacket(SetEnvelopeNamePacket.Type.TO, newText));
        });
        this.children.add(this.toField);

        this.fromField = new EditBox(this.font, i + 111, j + 84, 58, 10, new TranslatableComponent("container.snailmail.envelope.textfield.from")) {
            @Override
            public boolean charTyped(char c, int p_charTyped_2_) {
                if(!this.canConsumeInput()) {
                    return false;
                } else if(RandomUtil.isAllowedCharacter(c, false)) {
                    this.insertText(Character.toString(c));

                    return true;
                } else {
                    return false;
                }
            }

        };
        this.fromField.setValue(this.menu.clientStartFromName);
        this.fromField.setCanLoseFocus(true);
        this.fromField.setTextColor(0xFFFFFF);
        this.fromField.setTextColorUneditable(0xFFFFFF);
        this.fromField.setBordered(false);
        this.fromField.setMaxLength(35);
        this.fromField.setResponder(newText -> {
            ModNetwork.HANDLER.sendToServer(new SetEnvelopeNamePacket(SetEnvelopeNamePacket.Type.FROM, newText));
        });
        this.children.add(this.fromField);
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String s = this.toField.getValue();
        String s2 = this.fromField.getValue();
        this.init(mc, x, y);
        this.toField.setValue(s);
        this.fromField.setValue(s2);
    }

    @Override
    public void tick() {
        this.toField.tick();
        this.fromField.tick();
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if(key == 256) {
            this.onClose();
        }
        if(toField.isFocused()) {
            if(!this.toField.keyPressed(key, a, b) && !this.toField.canConsumeInput()) {
                return super.keyPressed(key, a, b);
            } else {
                return true;
            }
        } else if(fromField.isFocused()) {
            if(!this.fromField.keyPressed(key, a, b) && !this.fromField.canConsumeInput()) {
                return super.keyPressed(key, a, b);
            } else {
                return true;
            }
        }
        return super.keyPressed(key, a, b);
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, x, y, partialTicks);
        this.toField.render(stack, x, y, partialTicks);
        this.fromField.render(stack, x, y, partialTicks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int x, int y) {
        int xStart = (this.width - this.imageWidth) / 2;
        int yStart = (this.height - this.imageHeight) / 2;
        Minecraft.getInstance().getTextureManager().bind(GUI_TEXTURE);
        this.blit(stack, xStart, yStart, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack stack, int x, int y) {
        this.font.draw(stack, this.title, 8, 11, 0x404040);
        this.font.draw(stack, this.inventory.getDisplayName(), 8, 84, 0x404040);
    }

    @Override
    public void onClose() {
        BlockEntity target = Minecraft.getInstance().player.level.getBlockEntity(menu.returnPos);
        if (target instanceof SnailBoxBlockEntity) {
            ModNetwork.HANDLER.sendToServer(new OpenSnailBoxGUIPacket(menu.returnPos));
        } else {
            super.onClose();
        }
    }
}