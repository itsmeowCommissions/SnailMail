package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.util.BoxData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SnailBoxSelectionScreen extends Screen {
    private static final ITextComponent TITLE = new TranslationTextComponent("container.snailmail.snail_box");

    public BoxData[] boxes;
    private SnailBoxListWidget list;
    private SnailBoxScreen parent;

    public SnailBoxSelectionScreen(SnailBoxScreen parent, BoxData[] boxes) {
        super(TITLE);
        this.parent = parent;
        this.boxes = boxes;
    }

    @Override
    protected void init() {
        super.init();
        list = new SnailBoxListWidget(this);
        this.children.add(list);
        this.addButton(new Button((this.width - 200) / 2, this.height - 25, 200, 20, I18n.format("modal.snailmail.select"), btn -> {
            if(list.getSelected() != null) {
                this.finishSelection(list.getSelected().getBox());
            }
        }));
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        list.render(x, y, partialTicks);
        super.render(x, y, partialTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.displayGuiScreen(parent);
    }

    public void finishSelection(BoxData box) {
        this.minecraft.displayGuiScreen(parent);
        parent.finishSelection(box);
    }

    public FontRenderer getFontRenderer() {
        return this.font;
    }
}