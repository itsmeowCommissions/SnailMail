package net.examplemod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import net.examplemod.init.ModItems;
import net.examplemod.init.ModNetwork;
import net.examplemod.menu.SnailBoxMenu;
import net.examplemod.network.OpenEnvelopeGUIPacket;
import net.examplemod.network.SendEnvelopePacket;
import net.examplemod.network.UpdateSnailBoxPacket;
import net.examplemod.util.BoxData;
import net.examplemod.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SnailBoxScreen extends AbstractContainerScreen<SnailBoxMenu> implements IEnvelopePacketReceiver {
    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("snailmail:textures/gui/snail_box.png");
    public static final ResourceLocation CHECK_TEXTURE = new ResourceLocation("snailmail:textures/gui/checkbox.png");
    private EditBox nameField;
    private Button envelopeButton;

    public SnailBoxScreen(SnailBoxMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 199;
    }

    @Override
    protected void init() {
        super.init();
        int xStart = (this.width - this.imageWidth) / 2;
        int yStart = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(new Button(xStart + 84, yStart + 4, 67, 20, Component.translatable("container.snailmail.snail_box.send"), (bt) -> {
            ItemStack envelope = this.menu.getSlot(27).getItem();
            if(!envelope.isEmpty() && envelope.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                SendEnvelopePacket packet = new SendEnvelopePacket(SendEnvelopePacket.Type.TO_SERVER);
                this.receivePacket(packet);
                ModNetwork.HANDLER.sendToServer(packet);
            }
        }));
        this.envelopeButton = new Button(xStart + this.imageWidth - 80, yStart - 20, 80, 20, Component.translatable("container.snailmail.snail_box.open_envelope"), (bt) -> {
            ItemStack envelope = this.menu.getSlot(27).getItem();
            if (envelope.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                ModNetwork.HANDLER.sendToServer(new OpenEnvelopeGUIPacket(menu.pos));
            }
        });
        this.envelopeButton.active = this.menu.getSlot(27).getItem().getItem() == ModItems.ENVELOPE_OPEN.get();
        this.addRenderableWidget(envelopeButton);
        if(this.menu.isOwner) {
            this.addRenderableWidget(new Button(xStart + 88, yStart + 95, 82, 20, Component.translatable("container.snailmail.snail_box.members"), (bt) -> {
                Minecraft.getInstance().setScreen(new SnailBoxMemberScreen(this));
            }));
            Checkbox button = new Checkbox(xStart + 7, yStart + 82, 79, 14, Component.translatable("container.snailmail.snail_box.public"), this.menu.isPublic) {

                @Override
                public void onPress() {
                    super.onPress();
                    ModNetwork.HANDLER.sendToServer(new UpdateSnailBoxPacket(this.selected()));
                    SnailBoxScreen.this.menu.isPublic = this.selected();
                }

                @Override
                public void renderButton(PoseStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShaderTexture(0, CHECK_TEXTURE);
                    GuiComponent.blit(stack, this.x, this.y, this.selected() ? 14 : 0, 0, 14, 14, 28, 14);
                    stack.pushPose();
                    stack.scale(0.8F, 0.8F, 1F);
                    SnailBoxScreen.this.font.draw(stack, this.getMessage(), (this.x + 14 + 4) * 1.25F, (this.y + 4) * 1.25F, 0xFF404040);
                    stack.popPose();
                }

            };
            this.addRenderableWidget(button);
        }
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        this.nameField = new EditBox(this.font, xStart + 88, yStart + 83, 82, 10, Component.translatable("container.snailmail.snail_box.textfield.name")) {

            @Override
            public boolean charTyped(char c, int p_charTyped_2_) {
                if(!this.canConsumeInput()) {
                    return false;
                } else if(RandomUtil.isAllowedCharacter(c, true)) {
                    this.insertText(Character.toString(c));

                    return true;
                } else {
                    return false;
                }
            }

        };
        this.nameField.setValue(this.menu.startingName);
        this.nameField.setCanLoseFocus(true);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setTextColorUneditable(0xFFFFFF);
        this.nameField.setBordered(true);
        this.nameField.setMaxLength(35);
        this.nameField.setResponder(newText -> {
            ModNetwork.HANDLER.sendToServer(new UpdateSnailBoxPacket(newText));
            // when returning to the menu from another, will reset unless this is set
            this.menu.startingName = newText;
        });
        this.nameField.setVisible(menu.isOwner);
        this.addRenderableWidget(this.nameField);
    }

    @Override
    protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T guiEventListener) {
        if(Platform.isForge() && Platform.isModLoaded("quark")) {
            if(SnailBoxScreen.checkButton(guiEventListener)) {
                return null;
            }
        }
        return super.addRenderableWidget(guiEventListener);
    }

    @ExpectPlatform
    public static <T extends GuiEventListener & Widget & NarratableEntry> boolean checkButton(T guiEventListener) {
        return true;
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String s = this.nameField.getValue();
        this.init(mc, x, y);
        this.nameField.setValue(s);
    }

    @Override
    public void containerTick() {
        this.nameField.tick();
        this.envelopeButton.active = this.menu.getSlot(27).getItem().getItem() == ModItems.ENVELOPE_OPEN.get();
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if(key == 256) {
            Minecraft.getInstance().player.closeContainer();
        }
        if(nameField.isFocused()) {
            if(!this.nameField.keyPressed(key, a, b) && !this.nameField.canConsumeInput()) {
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
        this.nameField.render(stack, x, y, partialTicks);
        this.renderTooltip(stack, x, y);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        int xStart = (this.width - this.imageWidth) / 2;
        int yStart = (this.height - this.imageHeight) / 2;
        this.blit(stack, xStart, yStart, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title, 8, 11, 0x404040);
        this.font.draw(stack, this.playerInventoryTitle, 8, 104, 0x404040);
    }

    @Override
    public void receivePacket(SendEnvelopePacket msg) {
        if(msg.type == SendEnvelopePacket.Type.SELECT_BOX) {
            Minecraft.getInstance().setScreen(new SnailBoxSelectionScreen(this, msg.boxes));
        } else {
            Minecraft.getInstance().setScreen(new SnailBoxModalScreen(this, msg.type));
        }
    }

    public void finishSelection(BoxData box) {
        SendEnvelopePacket packet = new SendEnvelopePacket(SendEnvelopePacket.Type.TO_SERVER, box);
        this.receivePacket(packet);
        ModNetwork.HANDLER.sendToServer(packet);
    }

}
