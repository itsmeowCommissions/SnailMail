package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.init.ModNetwork;
import dev.itsmeow.snailmail.network.UpdateSnailBoxPacket;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SnailBoxMemberScreen extends Screen {
    private static final Component TITLE = new TranslatableComponent("container.snailmail.snail_box");

    private SnailBoxMemberListWidget list;
    public SnailBoxScreen parent;
    private EditBox nameField;

    public SnailBoxMemberScreen(SnailBoxScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        list = new SnailBoxMemberListWidget(this);
        this.addRenderableWidget(list);

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        this.nameField = new EditBox(this.font, (this.width - 100) / 2 - 75, 3, 100, 18, new TranslatableComponent("container.snailmail.snail_box.textfield.name")) {

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
        this.nameField.setCanLoseFocus(true);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setTextColorUneditable(0xFFFFFF);
        this.nameField.setBordered(true);
        this.nameField.setMaxLength(35);
        this.addRenderableWidget(this.nameField);

        this.addRenderableWidget(new Button((this.width - 100) / 2 + 75, 2, 100, 20, new TranslatableComponent("modal.snailmail.add"), btn -> {
            if(!this.nameField.getValue().isEmpty()) {
                ModNetwork.HANDLER.sendToServer(new UpdateSnailBoxPacket(this.nameField.getValue(), true));
            }
        }));
        this.addRenderableWidget(new Button((this.width - 150) / 2, this.height - 50, 150, 20, new TranslatableComponent("modal.snailmail.remove_selected"), btn -> {
            if(this.list != null && this.list.getSelected() != null) {
                ModNetwork.HANDLER.sendToServer(new UpdateSnailBoxPacket(list.getSelected().getNameOrId(), false));
            }
        }));
        this.addRenderableWidget(new Button((this.width - 200) / 2, this.height - 25, 200, 20, new TranslatableComponent("modal.snailmail.done"), btn -> {
            Minecraft.getInstance().setScreen(parent);
        }));
    }

    @Override
    public void resize(Minecraft mc, int x, int y) {
        String s = this.nameField.getValue();
        this.init(mc, x, y);
        this.nameField.setValue(s);
    }

    @Override
    public void tick() {
        this.nameField.tick();
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if(key == 256) {
            Minecraft.getInstance().setScreen(parent);
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
        list.render(stack, x, y, partialTicks);
        super.render(stack, x, y, partialTicks);
        this.nameField.render(stack, x, y, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    public Font getFontRenderer() {
        return this.font;
    }

    public void failedAdd() {
        Minecraft.getInstance().setScreen(new SnailBoxMemberPopupScreen(this));
    }

    public void refreshList() {
        if(this.list != null) {
            this.list.refreshList();
        }
    }
}