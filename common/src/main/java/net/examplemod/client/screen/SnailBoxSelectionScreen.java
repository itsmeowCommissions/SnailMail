package net.examplemod.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.examplemod.util.BoxData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SnailBoxSelectionScreen extends Screen {
    private static final Component TITLE = Component.translatable("container.snailmail.snail_box");

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
        this.addRenderableWidget(list);
        this.addRenderableWidget(new Button((this.width - 200) / 2, this.height - 25, 200, 20, Component.translatable("modal.snailmail.select"), btn -> {
            if(list.getSelected() != null) {
                this.finishSelection(list.getSelected().getBox());
            }
        }));
    }

    @Override
    public void render(PoseStack stack, int x, int y, float partialTicks) {
        list.render(stack, x, y, partialTicks);
        super.render(stack, x, y, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    public void finishSelection(BoxData box) {
        Minecraft.getInstance().setScreen(parent);
        parent.finishSelection(box);
    }

    public Font getFontRenderer() {
        return this.font;
    }
}
