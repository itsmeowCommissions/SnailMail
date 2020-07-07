package dev.itsmeow.snailmail.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import dev.itsmeow.snailmail.entity.SnailManEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class SnailManModel extends EntityModel<SnailManEntity> {
    public ModelRenderer body1;
    public ModelRenderer shell;
    public ModelRenderer tail;
    public ModelRenderer body2;
    public ModelRenderer harness;
    public ModelRenderer chestLeftSmall;
    public ModelRenderer chestLeftBig;
    public ModelRenderer chestRightSmall;
    public ModelRenderer chestRightBig;
    public ModelRenderer chestBack;
    public ModelRenderer seat;
    public ModelRenderer chestFront;
    public ModelRenderer flagPole;
    public ModelRenderer flag;
    public ModelRenderer head;
    public ModelRenderer strapLeft;
    public ModelRenderer strapRight;
    public ModelRenderer eyeLeft;
    public ModelRenderer eyeRight;
    public ModelRenderer nubRight;
    public ModelRenderer nubLeft;

    public SnailManModel() {
        this.textureWidth = 180;
        this.textureHeight = 128;
        this.strapLeft = new ModelRenderer(this, 0, 56);
        this.strapLeft.setRotationPoint(3.0F, -3.5F, -8.0F);
        this.strapLeft.addBox(0.0F, -6.0F, -0.5F, 0, 6, 13, 0.0F);
        this.setRotateAngle(strapLeft, 0.0F, -0.17453292519943295F, 0.0F);
        this.tail = new ModelRenderer(this, 0, 50);
        this.tail.setRotationPoint(0.0F, 1.0F, 7.5F);
        this.tail.addBox(-4.5F, -2.5F, 0.0F, 9, 5, 14, 0.0F);
        this.eyeLeft = new ModelRenderer(this, 0, 33);
        this.eyeLeft.setRotationPoint(3.0F, -3.0F, -10.0F);
        this.eyeLeft.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 9, 0.0F);
        this.setRotateAngle(eyeLeft, -0.8726646259971648F, -0.4363323129985824F, 0.0F);
        this.chestRightBig = new ModelRenderer(this, 128, 41);
        this.chestRightBig.setRotationPoint(-9.0F, -7.5F, 0.5F);
        this.chestRightBig.addBox(-7.0F, 0.0F, -8.0F, 7, 10, 16, 0.0F);
        this.setRotateAngle(chestRightBig, 0.12217304763960307F, 0.0F, 0.0F);
        this.flag = new ModelRenderer(this, 64, 51);
        this.flag.setRotationPoint(0.0F, -7.5F, 0.5F);
        this.flag.addBox(0.0F, -5.0F, 0.0F, 0, 10, 21, 0.0F);
        this.chestLeftSmall = new ModelRenderer(this, 132, 22);
        this.chestLeftSmall.setRotationPoint(9.0F, -17.0F, 0.0F);
        this.chestLeftSmall.addBox(0.0F, 0.0F, -6.0F, 5, 7, 12, 0.0F);
        this.setRotateAngle(chestLeftSmall, 0.08726646259971647F, 0.0F, 0.0F);
        this.chestRightSmall = new ModelRenderer(this, 110, 38);
        this.chestRightSmall.setRotationPoint(-9.0F, -17.0F, 0.0F);
        this.chestRightSmall.addBox(-5.0F, 0.0F, -6.0F, 5, 7, 12, 0.0F);
        this.setRotateAngle(chestRightSmall, 0.08726646259971647F, 0.0F, 0.0F);
        this.strapRight = new ModelRenderer(this, 0, 56);
        this.strapRight.setRotationPoint(-3.0F, -3.5F, -8.0F);
        this.strapRight.addBox(0.0F, -6.0F, -0.5F, 0, 6, 13, 0.0F);
        this.setRotateAngle(strapRight, 0.0F, 0.17453292519943295F, 0.0F);
        this.harness = new ModelRenderer(this, 0, 69);
        this.harness.setRotationPoint(0.0F, -10.5F, 0.0F);
        this.harness.addBox(-9.5F, -13.0F, -13.0F, 19, 26, 26, 0.0F);
        this.eyeRight = new ModelRenderer(this, 0, 22);
        this.eyeRight.setRotationPoint(-3.0F, -3.0F, -10.0F);
        this.eyeRight.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 9, 0.0F);
        this.setRotateAngle(eyeRight, -0.8726646259971648F, 0.4363323129985824F, 0.0F);
        this.chestLeftBig = new ModelRenderer(this, 124, 80);
        this.chestLeftBig.setRotationPoint(9.0F, -7.5F, 0.5F);
        this.chestLeftBig.addBox(0.0F, 0.0F, -8.0F, 7, 10, 16, 0.0F);
        this.setRotateAngle(chestLeftBig, 0.12217304763960307F, 0.0F, 0.0F);
        this.chestFront = new ModelRenderer(this, 96, 82);
        this.chestFront.setRotationPoint(0.0F, -17.0F, -12.5F);
        this.chestFront.addBox(-8.0F, -3.0F, -6.0F, 16, 7, 6, 0.0F);
        this.body2 = new ModelRenderer(this, 46, 50);
        this.body2.setRotationPoint(0.0F, 0.0F, -7.5F);
        this.body2.addBox(-5.0F, -3.5F, -10.0F, 10, 7, 10, 0.0F);
        this.chestBack = new ModelRenderer(this, 120, 67);
        this.chestBack.setRotationPoint(0.0F, -13.5F, 12.5F);
        this.chestBack.addBox(-8.0F, -3.0F, 0.0F, 16, 7, 6, 0.0F);
        this.setRotateAngle(chestBack, 0.06981317007977318F, 0.0F, 0.0F);
        this.head = new ModelRenderer(this, 88, 0);
        this.head.setRotationPoint(0.0F, 0.5F, -10.0F);
        this.head.addBox(-4.0F, -3.0F, -10.0F, 8, 6, 10, 0.0F);
        this.nubLeft = new ModelRenderer(this, 0, 4);
        this.nubLeft.setRotationPoint(3.0F, 1.0F, -10.0F);
        this.nubLeft.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F);
        this.flagPole = new ModelRenderer(this, 8, 0);
        this.flagPole.setRotationPoint(0.0F, -1.5F, -9.0F);
        this.flagPole.addBox(-0.5F, -13.5F, -0.5F, 1, 14, 1, 0.0F);
        this.setRotateAngle(flagPole, 0.17453292519943295F, 0.0F, 0.0F);
        this.seat = new ModelRenderer(this, 73, 54);
        this.seat.setRotationPoint(0.0F, -23.0F, 6.0F);
        this.seat.addBox(-6.0F, -1.5F, -11.0F, 12, 2, 13, 0.0F);
        this.nubRight = new ModelRenderer(this, 0, 0);
        this.nubRight.setRotationPoint(-3.0F, 1.0F, -10.0F);
        this.nubRight.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F);
        this.body1 = new ModelRenderer(this, 0, 0);
        this.body1.setRotationPoint(0.0F, 20.5F, 0.0F);
        this.body1.addBox(-5.5F, -3.5F, -7.5F, 11, 7, 15, 0.0F);
        this.shell = new ModelRenderer(this, 27, 0);
        this.shell.setRotationPoint(0.0F, -3.5F, 3.0F);
        this.shell.addBox(-9.0F, -23.0F, -12.5F, 18, 25, 25, 0.0F);
        this.setRotateAngle(shell, -0.12217304763960307F, 0.0F, 0.0F);
        this.body2.addChild(this.strapLeft);
        this.body1.addChild(this.tail);
        this.head.addChild(this.eyeLeft);
        this.shell.addChild(this.chestRightBig);
        this.flagPole.addChild(this.flag);
        this.shell.addChild(this.chestLeftSmall);
        this.shell.addChild(this.chestRightSmall);
        this.body2.addChild(this.strapRight);
        this.shell.addChild(this.harness);
        this.head.addChild(this.eyeRight);
        this.shell.addChild(this.chestLeftBig);
        this.shell.addChild(this.chestFront);
        this.body1.addChild(this.body2);
        this.shell.addChild(this.chestBack);
        this.body2.addChild(this.head);
        this.head.addChild(this.nubLeft);
        this.seat.addChild(this.flagPole);
        this.shell.addChild(this.seat);
        this.head.addChild(this.nubRight);
        this.body1.addChild(this.shell);
    }

    @Override
    public void render(MatrixStack arg0, IVertexBuilder arg1, int arg2, int arg3, float arg4, float arg5, float arg6, float arg7) {
        this.body1.render(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    @Override
    public void setRotationAngles(SnailManEntity arg0, float arg1, float arg2, float arg3, float arg4, float arg5) {

    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
