package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.network.SendEnvelopePacket;
import dev.itsmeow.snailmail.network.SendEnvelopePacket.Type;
import dev.itsmeow.snailmail.network.UpdateSnailBoxPacket;
import dev.itsmeow.snailmail.util.BoxData;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SnailBoxScreen extends ContainerScreen<SnailBoxContainer> implements IEnvelopePacketReceiver {
    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("snailmail:textures/gui/snail_box.png");
    public static final ResourceLocation CHECK_TEXTURE = new ResourceLocation("snailmail:textures/gui/checkbox.png");
    private TextFieldWidget nameField;

    public SnailBoxScreen(SnailBoxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 176;
        this.ySize = 199;
    }

    @Override
    protected void init() {
        super.init();
        int xStart = (this.width - this.xSize) / 2;
        int yStart = (this.height - this.ySize) / 2;
        this.addButton(new Button(xStart + 84, yStart + 4, 67, 20, I18n.format("container.snailmail.snail_box.send"), (bt) -> {
            ItemStack envelope = this.container.getSlot(27).getStack();
            if(!envelope.isEmpty() && envelope.getItem() == ModItems.ENVELOPE_OPEN) {
                SendEnvelopePacket packet = new SendEnvelopePacket(Type.TO_SERVER);
                this.receivePacket(packet);
                SnailMail.HANDLER.sendToServer(packet);
            }
        }));
        if(this.container.isOwner) {
            this.addButton(new Button(xStart + 88, yStart + 95, 82, 20, I18n.format("container.snailmail.snail_box.members"), (bt) -> {
                this.minecraft.displayGuiScreen(new SnailBoxMemberScreen(this));
            }));
            CheckboxButton button = new CheckboxButton(xStart + 7, yStart + 82, 79, 14, I18n.format("container.snailmail.snail_box.public"), this.container.isPublic) {

                @Override
                public void onPress() {
                    super.onPress();
                    SnailMail.HANDLER.sendToServer(new UpdateSnailBoxPacket(this.isChecked()));
                    SnailBoxScreen.this.container.isPublic = this.isChecked();
                }

                @Override
                public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.getTextureManager().bindTexture(CHECK_TEXTURE);
                    blit(this.x, this.y, this.isChecked() ? 14 : 0, 0, 14, 14, 28, 14);
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(0.8F, 0.8F, 1F);
                    minecraft.fontRenderer.drawString(this.getMessage(), (this.x + 14 + 4) * 1.25F, (this.y + 4) * 1.25F, 0xFF404040);
                    RenderSystem.popMatrix();
                }

            };
            this.addButton(button);
        }
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.nameField = new TextFieldWidget(this.font, xStart + 88, yStart + 83, 82, 10, I18n.format("container.snailmail.snail_box.textfield.name")) {

            @Override
            public boolean charTyped(char c, int p_charTyped_2_) {
                if(!this.canWrite()) {
                    return false;
                } else if(RandomUtil.isAllowedCharacter(c, true)) {
                    this.writeText(Character.toString(c));

                    return true;
                } else {
                    return false;
                }
            }

        };
        this.nameField.setText(this.container.startingName);
        this.nameField.setCanLoseFocus(true);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setDisabledTextColour(0xFFFFFF);
        this.nameField.setEnableBackgroundDrawing(true);
        this.nameField.setMaxStringLength(35);
        this.nameField.setResponder(newText -> {
            SnailMail.HANDLER.sendToServer(new UpdateSnailBoxPacket(newText));
            // when returning to the menu from another, will reset unless this is set
            this.container.startingName = newText;
        });
        this.nameField.setVisible(container.isOwner);
        this.children.add(this.nameField);
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String s = this.nameField.getText();
        this.init(mc, x, y);
        this.nameField.setText(s);
    }

    @Override
    public void tick() {
        this.nameField.tick();
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if(key == 256) {
            this.minecraft.player.closeScreen();
        }
        if(nameField.isFocused()) {
            if(!this.nameField.keyPressed(key, a, b) && !this.nameField.canWrite()) {
                return super.keyPressed(key, a, b);
            } else {
                return true;
            }
        }
        return super.keyPressed(key, a, b);
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.renderBackground();
        super.render(x, y, partialTicks);
        this.nameField.render(x, y, partialTicks);
        this.renderHoveredToolTip(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int xStart = (this.width - this.xSize) / 2;
        int yStart = (this.height - this.ySize) / 2;
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        this.blit(xStart, yStart, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getFormattedText(), 8, 11, 0x404040);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, 104, 0x404040);
    }

    @Override
    public void receivePacket(SendEnvelopePacket msg) {
        if(msg.type == Type.SELECT_BOX) {
            this.minecraft.displayGuiScreen(new SnailBoxSelectionScreen(this, msg.boxes));
        } else {
            this.minecraft.displayGuiScreen(new SnailBoxModalScreen(this, msg.type));
        }
    }

    public void finishSelection(BoxData box) {
        SendEnvelopePacket packet = new SendEnvelopePacket(Type.TO_SERVER, box);
        this.receivePacket(packet);
        SnailMail.HANDLER.sendToServer(packet);
    }

}