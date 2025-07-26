package dev.itsmeow.snailmail.client.screen;

import dev.architectury.platform.Platform;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.init.ModNetwork;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.network.OpenEnvelopeGUIPacket;
import dev.itsmeow.snailmail.network.OpenSnailBoxGUIPacket;
import dev.itsmeow.snailmail.network.SetEnvelopeNamePacket;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnvelopeScreen extends AbstractContainerScreen<EnvelopeMenu> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("snailmail:textures/gui/envelope_open.png");
    private EditBox toField;
    private EditBox fromField;
    private Button closeButton;

    public EnvelopeScreen(EnvelopeMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 178;
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.closeButton = Button.builder(Component.translatable("container.snailmail.envelope.close"), (bt) -> {
            EnvelopeScreen.this.onClose();
        }).pos(i + this.imageWidth - 80, j - 20).size(80, 20).build();
        this.addRenderableWidget(closeButton);
        this.toField = new EditBox(this.font, i + 92, j + 10, 58, 10, Component.translatable("container.snailmail.envelope.textfield.to")) {

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
        this.addRenderableWidget(this.toField);

        this.fromField = new EditBox(this.font, i + 111, j + 84, 58, 10, Component.translatable("container.snailmail.envelope.textfield.from")) {
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
        this.addRenderableWidget(this.fromField);
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
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener) {
        if(Platform.isNeoForge() && Platform.isModLoaded("quark")) {
            if(SnailBoxScreen.checkButton(guiEventListener)) {
                return null;
            }
        }
        return super.addRenderableWidget(guiEventListener);
    }

    @Override
    public void containerTick() {
        this.toField.tick();
        this.fromField.tick();
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
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, x, y, partialTicks);
        this.toField.render(guiGraphics, x, y, partialTicks);
        this.fromField.render(guiGraphics, x, y, partialTicks);
        this.renderTooltip(guiGraphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        int xStart = (this.width - this.imageWidth) / 2;
        int yStart = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, xStart, yStart, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawString(this.font, this.title, 8, 11, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, 84, 0x404040, false);
    }

    @Override
    public void onClose() {
        BlockEntity target = Minecraft.getInstance().player.level().getBlockEntity(menu.returnPos);
        if (target instanceof SnailBoxBlockEntity) {
            ModNetwork.HANDLER.sendToServer(new OpenSnailBoxGUIPacket(menu.returnPos));
        } else {
            super.onClose();
        }
    }
}