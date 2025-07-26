package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.itsmeow.snailmail.util.BoxData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SnailBoxListWidget extends ObjectSelectionList<SnailBoxListWidget.BoxEntry> {

    private final int listWidth;
    private SnailBoxSelectionScreen parent;

    public SnailBoxListWidget(SnailBoxSelectionScreen parent) {
        super(Minecraft.getInstance(), 256, parent.height, 0, parent.height - 30, parent.getFontRenderer().lineHeight * 2 + 8);
        this.x0 = (parent.width - 256) / 2;
        this.x1 = this.x0 + 256;
        this.parent = parent;
        this.listWidth = 256;
        this.refreshList();
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth;
    }

    public void refreshList() {
        this.clearEntries();
        for(BoxData box : parent.boxes) {
            this.addEntry(new BoxEntry(box));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        this.renderBackground(guiGraphics);

        int scrollbarX = this.getScrollbarPosition();
        int scrollbarXEnd = scrollbarX + 6;
        this.enableScissor(guiGraphics);
        this.renderList(guiGraphics, mouseX, mouseY, f);
        guiGraphics.disableScissor();
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int height = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            height = Mth.clamp(height, 32, this.y1 - this.y0 - 8);
            int scrollOffset = (int)this.getScrollAmount() * (this.y1 - this.y0 - height) / maxScroll + this.y0;
            if (scrollOffset < this.y0) {
                scrollOffset = this.y0;
            }
            guiGraphics.fill(scrollbarX, this.y0, scrollbarXEnd, this.y1, -16777216);
            guiGraphics.fill(scrollbarX, scrollOffset, scrollbarXEnd, scrollOffset + height, -8355712);
            guiGraphics.fill(scrollbarX, scrollOffset, scrollbarXEnd - 1, scrollOffset + height - 1, -4144960);
        }

        this.renderDecorations(guiGraphics, mouseX, mouseY);
        RenderSystem.disableBlend();

        guiGraphics.fill(this.x0 - 6, parent.height - 30, this.x0 + 256 + 6, parent.height, 0xFF606060);
        guiGraphics.fill(this.x0 - 6, parent.height - 30, this.x0 + 256 + 6, parent.height - 29, 0xFF000000);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        parent.renderBackground(guiGraphics);
        guiGraphics.fill(this.x0 - 6, 0, this.x0 + 256 + 6, parent.height, 0xFF404040);
        guiGraphics.fill(this.x0 - 7, 0, this.x0 - 6, parent.height, 0xFF000000);
        guiGraphics.fill(this.x0 + 256 + 6, 0, this.x0 + 256 + 7, parent.height, 0xFF000000);
    }

    public class BoxEntry extends ObjectSelectionList.Entry<BoxEntry> {
        private final BoxData box;
        private String locStringL = "";
        private Component locStringComponent;

        BoxEntry(BoxData box) {
            this.box = box;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            guiGraphics.fill(left, top, left + entryWidth - 4, top + entryHeight, 0xFF303030);
            Font font = SnailBoxListWidget.this.parent.getFontRenderer();
            guiGraphics.drawString(font, font.plainSubstrByWidth(box.name, 256), left + 3, top + 2, 0xFFFFFF, false);
            String locString = "";
            if(box.pos != null) {
                String posString = "X" + box.pos.getX() + " Y" + box.pos.getY() + " Z" + box.pos.getZ();
                locString = I18n.get("modal.snailmail.located", posString, box.pos.getDimension().location().toString().replaceFirst("minecraft:", ""));
            } else {
                locString = I18n.get("modal.snailmail.no_location");
            }
            if(!locString.equals(locStringL)) {
                locStringL = locString;
                locStringComponent = Component.literal(locString);
            }
            guiGraphics.drawString(font, font.plainSubstrByWidth(locString, 256), left + 3, top + 2 + font.lineHeight, 0xCCCCCC, false);
            String s;
            if(box.member) {
                s = I18n.get("modal.snailmail.member");
            } else {
                s = I18n.get("modal.snailmail.owner");
            }
            guiGraphics.drawString(font, s, left + 250 - font.width(s), top + 2, 0x55FF55, false);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            SnailBoxListWidget.this.setSelected(this);
            return false;
        }

        public BoxData getBox() {
            return this.box;
        }

        @Override
        public Component getNarration() {
            return locStringComponent;
        }
    }
}