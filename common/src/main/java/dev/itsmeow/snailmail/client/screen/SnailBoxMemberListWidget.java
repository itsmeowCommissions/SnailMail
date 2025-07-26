package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SnailBoxMemberListWidget extends ObjectSelectionList<SnailBoxMemberListWidget.MemberEntry> {

    private final int listWidth;
    private SnailBoxMemberScreen parent;

    public SnailBoxMemberListWidget(SnailBoxMemberScreen parent) {
        super(Minecraft.getInstance(), 256, parent.height, 25, parent.height - 55, parent.getFontRenderer().lineHeight + 8);
        this.x0 = (parent.width - 256) / 2;
        this.x1 = x0 + 256;
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
        for(String name : parent.parent.getMenu().memberUsers) {
            this.addEntry(new MemberEntry(name));
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

        guiGraphics.fill(this.x0 - 6, parent.height - 55, this.x0 + 256 + 6, parent.height, 0xFF606060);
        guiGraphics.fill(this.x0 - 6, parent.height - 55, this.x0 + 256 + 6, parent.height - 54, 0xFF000000);
        guiGraphics.fill(this.x0 - 6, 0, this.x0 + 256 + 6, 24, 0xFF606060);
        guiGraphics.fill(this.x0 - 6, 25, this.x0 + 256 + 6, 25, 0xFF000000);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        parent.renderBackground(guiGraphics);
        guiGraphics.fill(this.x0 - 6, 0, this.x0 + 256 + 6, parent.height, 0xFF404040);
        guiGraphics.fill(this.x0 - 7, 0, this.x0 - 6, parent.height, 0xFF000000);
        guiGraphics.fill(this.x0 + 256 + 6, 0, this.x0 + 256 + 7, parent.height, 0xFF000000);
    }

    public class MemberEntry extends ObjectSelectionList.Entry<MemberEntry> {
        private final String nameOrId;
        private final Component nameComponent;

        MemberEntry(String nameOrId) {
            this.nameOrId = nameOrId;
            this.nameComponent = Component.literal(nameOrId);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            guiGraphics.fill(left, top, left + entryWidth - 4, top + entryHeight, 0xFF303030);
            Font font = SnailBoxMemberListWidget.this.parent.getFontRenderer();
            guiGraphics.drawString(font, font.plainSubstrByWidth(nameOrId, 256), left + 3, top + 2, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            SnailBoxMemberListWidget.this.setSelected(this);
            return false;
        }

        public String getNameOrId() {
            return nameOrId;
        }

        @Override
        public Component getNarration() {
            return nameComponent;
        }
    }
}