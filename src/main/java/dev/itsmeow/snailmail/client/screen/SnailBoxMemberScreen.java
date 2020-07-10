package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.network.UpdateSnailBoxPacket;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SnailBoxMemberScreen extends Screen {
    private static final ITextComponent TITLE = new TranslationTextComponent("container.snailmail.snail_box");

    private SnailBoxMemberListWidget list;
    public SnailBoxScreen parent;
    private TextFieldWidget nameField;

    public SnailBoxMemberScreen(SnailBoxScreen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        list = new SnailBoxMemberListWidget(this);
        this.children.add(list);

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.nameField = new TextFieldWidget(this.font, (this.width - 100) / 2 - 75, 3, 100, 18, I18n.format("container.snailmail.snail_box.textfield.name")) {

            @Override
            public boolean charTyped(char c, int p_charTyped_2_) {
                if(!this.canWrite()) {
                    return false;
                } else if(RandomUtil.isAllowedCharacter(c, false)) {
                    this.writeText(Character.toString(c));

                    return true;
                } else {
                    return false;
                }
            }

        };
        this.nameField.setCanLoseFocus(true);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setDisabledTextColour(0xFFFFFF);
        this.nameField.setEnableBackgroundDrawing(true);
        this.nameField.setMaxStringLength(35);
        this.children.add(this.nameField);

        this.addButton(new Button((this.width - 100) / 2 + 75, 2, 100, 20, I18n.format("modal.snailmail.add"), btn -> {
            if(!this.nameField.getText().isEmpty()) {
                SnailMail.HANDLER.sendToServer(new UpdateSnailBoxPacket(this.nameField.getText(), true));
            }
        }));
        this.addButton(new Button((this.width - 150) / 2, this.height - 50, 150, 20, I18n.format("modal.snailmail.remove_selected"), btn -> {
            if(this.list != null && this.list.getSelected() != null) {
                SnailMail.HANDLER.sendToServer(new UpdateSnailBoxPacket(list.getSelected().getNameOrId(), false));
            }
        }));
        this.addButton(new Button((this.width - 200) / 2, this.height - 25, 200, 20, I18n.format("modal.snailmail.done"), btn -> {
            this.minecraft.displayGuiScreen(parent);
        }));
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
            this.minecraft.displayGuiScreen(parent);
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
        list.render(x, y, partialTicks);
        super.render(x, y, partialTicks);
        this.nameField.render(x, y, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.displayGuiScreen(parent);
    }

    public FontRenderer getFontRenderer() {
        return this.font;
    }

    public void failedAdd() {
        this.minecraft.displayGuiScreen(new SnailBoxMemberPopupScreen(this));
    }

    public void refreshList() {
        if(this.list != null) {
            this.list.refreshList();
        }
    }
}