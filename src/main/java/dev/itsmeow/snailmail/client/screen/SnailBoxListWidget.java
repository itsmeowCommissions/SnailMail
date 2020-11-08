package dev.itsmeow.snailmail.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.itsmeow.snailmail.util.BoxData;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class SnailBoxListWidget extends ExtendedList<SnailBoxListWidget.BoxEntry> {

    public static final ResourceLocation MODAL_BIG_TEXTURE = new ResourceLocation("snailmail:textures/gui/modal_big.png");
    private final int listWidth;
    private SnailBoxSelectionScreen parent;

    public SnailBoxListWidget(SnailBoxSelectionScreen parent) {
        super(parent.getMinecraft(), 256, parent.height, 0, parent.height - 30, parent.getFontRenderer().FONT_HEIGHT * 2 + 8);
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
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(stack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        int k = this.getRowLeft();
        int l = this.y0 + 4 - (int) this.getScrollAmount();
        this.renderList(stack, k, l, p_render_1_, p_render_2_, p_render_3_);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        int j1 = Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
        if(j1 > 0) {
            int k1 = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
            int l1 = (int) this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
            if(l1 < this.y0) {
                l1 = this.y0;
            }
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double) i, (double) this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double) j, (double) this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double) j, (double) this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double) i, (double) this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double) i, (double) (l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double) j, (double) (l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double) j, (double) l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos((double) i, (double) l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double) i, (double) (l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double) (j - 1), (double) (l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double) (j - 1), (double) l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((double) i, (double) l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        AbstractGui.fill(stack, this.x0 - 6, parent.height - 30, this.x0 + 256 + 6, parent.height, 0xFF606060);
        AbstractGui.fill(stack, this.x0 - 6, parent.height - 30, this.x0 + 256 + 6, parent.height - 29, 0xFF000000);
    }

    @Override
    protected void renderBackground(MatrixStack stack) {
        parent.renderBackground(stack);
        AbstractGui.fill(stack, this.x0 - 6, 0, this.x0 + 256 + 6, parent.height, 0xFF404040);
        AbstractGui.fill(stack, this.x0 - 7, 0, this.x0 - 6, parent.height, 0xFF000000);
        AbstractGui.fill(stack, this.x0 + 256 + 6, 0, this.x0 + 256 + 7, parent.height, 0xFF000000);
    }

    public class BoxEntry extends ExtendedList.AbstractListEntry<BoxEntry> {
        private final BoxData box;

        BoxEntry(BoxData box) {
            this.box = box;
        }

        @Override
        public void render(MatrixStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean p_194999_5_, float partialTicks) {
            AbstractGui.fill(stack, left, top, left + entryWidth - 4, top + entryHeight, 0xFF303030);
            FontRenderer font = SnailBoxListWidget.this.parent.getFontRenderer();
            // func_238412_a_ = trimStringToWidth
            font.drawString(stack, font.func_238412_a_(box.name, 256), left + 3, top + 2, 0xFFFFFF);
            String locString = "";
            if(box.pos != null) {
                String posString = "X" + box.pos.getX() + " Y" + box.pos.getY() + " Z" + box.pos.getZ();
                locString = I18n.format("modal.snailmail.located", posString, box.pos.getDimension().getLocation().toString().replaceFirst("minecraft:", ""));
            } else {
                locString = I18n.format("modal.snailmail.no_location");
            }
            font.drawString(stack, font.func_238412_a_(locString, 256), left + 3, top + 2 + font.FONT_HEIGHT, 0xCCCCCC);
            String s;
            if(box.member) {
                s = I18n.format("modal.snailmail.member");
            } else {
                s = I18n.format("modal.snailmail.owner");
            }
            font.drawString(stack, s, left + 250 - font.getStringWidth(s), top + 2, 0x55FF55);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            SnailBoxListWidget.this.setSelected(this);
            return false;
        }

        public BoxData getBox() {
            return this.box;
        }
    }
}