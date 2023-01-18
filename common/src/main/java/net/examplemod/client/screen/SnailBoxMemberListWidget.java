package net.examplemod.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
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
    public void render(PoseStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(stack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int) this.getScrollAmount();
        //this.renderList(stack, k, l, p_render_1_, p_render_2_, p_render_3_);
        this.renderList(stack, k, l, p_render_1_);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableTexture();
        int j1 = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
        if(j1 > 0) {
            int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if(l1 < this.y0) {
                l1 = this.y0;
            }
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(i, this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(j, this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(j, this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(i, this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.end();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(i, (l1 + k1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(j, (l1 + k1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(j, l1, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            tessellator.end();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(i, (l1 + k1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((j - 1), (l1 + k1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((j - 1), l1, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex(i, l1, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.end();
        }
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        GuiComponent.fill(stack, this.x0 - 6, parent.height - 55, this.x0 + 256 + 6, parent.height, 0xFF606060);
        GuiComponent.fill(stack, this.x0 - 6, parent.height - 55, this.x0 + 256 + 6, parent.height - 54, 0xFF000000);
        GuiComponent.fill(stack, this.x0 - 6, 0, this.x0 + 256 + 6, 24, 0xFF606060);
        GuiComponent.fill(stack, this.x0 - 6, 25, this.x0 + 256 + 6, 25, 0xFF000000);
    }

    @Override
    protected void renderBackground(PoseStack stack) {
        parent.renderBackground(stack);
        GuiComponent.fill(stack, this.x0 - 6, 0, this.x0 + 256 + 6, parent.height, 0xFF404040);
        GuiComponent.fill(stack, this.x0 - 7, 0, this.x0 - 6, parent.height, 0xFF000000);
        GuiComponent.fill(stack, this.x0 + 256 + 6, 0, this.x0 + 256 + 7, parent.height, 0xFF000000);
    }

    public class MemberEntry extends ObjectSelectionList.Entry<MemberEntry> {
        private final String nameOrId;
        private final Component nameComponent;

        MemberEntry(String nameOrId) {
            this.nameOrId = nameOrId;
            this.nameComponent = Component.literal(nameOrId);
        }

        @Override
        public void render(PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            GuiComponent.fill(stack, left, top, left + entryWidth - 4, top + entryHeight, 0xFF303030);
            Font font = SnailBoxMemberListWidget.this.parent.getFontRenderer();
            // func_238412_a_ = trimStringToWidth
            font.draw(stack, font.plainSubstrByWidth(nameOrId, 256), left + 3, top + 2, 0xFFFFFF);
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
