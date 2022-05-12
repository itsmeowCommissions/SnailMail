package dev.itsmeow.snailmail.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class SnailManModel extends EntityModel<SnailManEntity> {
    public ModelPart body1;
    public ModelPart shell;
    public ModelPart tail;
    public ModelPart body2;
    public ModelPart harness;
    public ModelPart chestLeftSmall;
    public ModelPart chestLeftBig;
    public ModelPart chestRightSmall;
    public ModelPart chestRightBig;
    public ModelPart chestBack;
    public ModelPart seat;
    public ModelPart chestFront;
    public ModelPart flagPole;
    public ModelPart flag;
    public ModelPart head;
    public ModelPart strapLeft;
    public ModelPart strapRight;
    public ModelPart eyeLeft;
    public ModelPart eyeRight;
    public ModelPart nubRight;
    public ModelPart nubLeft;
    private float opacity;

    public SnailManModel() {
        this.texWidth = 180;
        this.texHeight = 128;
        this.strapLeft = new ModelPart(this, 0, 56);
        this.strapLeft.setPos(3.0F, -3.5F, -8.0F);
        this.strapLeft.addBox(0.0F, -6.0F, -0.5F, 0, 6, 13, 0.0F);
        this.setRotateAngle(strapLeft, 0.0F, -0.17453292519943295F, 0.0F);
        this.tail = new ModelPart(this, 0, 50);
        this.tail.setPos(0.0F, 1.0F, 7.5F);
        this.tail.addBox(-4.5F, -2.5F, 0.0F, 9, 5, 14, 0.0F);
        this.eyeLeft = new ModelPart(this, 0, 33);
        this.eyeLeft.setPos(3.0F, -3.0F, -10.0F);
        this.eyeLeft.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 9, 0.0F);
        this.setRotateAngle(eyeLeft, -0.8726646259971648F, -0.4363323129985824F, 0.0F);
        this.chestRightBig = new ModelPart(this, 128, 41);
        this.chestRightBig.setPos(-9.0F, -7.5F, 0.5F);
        this.chestRightBig.addBox(-7.0F, 0.0F, -8.0F, 7, 10, 16, 0.0F);
        this.setRotateAngle(chestRightBig, 0.12217304763960307F, 0.0F, 0.0F);
        this.flag = new ModelPart(this, 64, 51);
        this.flag.setPos(0.0F, -7.5F, 0.5F);
        this.flag.addBox(0.0F, -5.0F, 0.0F, 0, 10, 21, 0.0F);
        this.chestLeftSmall = new ModelPart(this, 132, 22);
        this.chestLeftSmall.setPos(9.0F, -17.0F, 0.0F);
        this.chestLeftSmall.addBox(0.0F, 0.0F, -6.0F, 5, 7, 12, 0.0F);
        this.setRotateAngle(chestLeftSmall, 0.08726646259971647F, 0.0F, 0.0F);
        this.chestRightSmall = new ModelPart(this, 110, 38);
        this.chestRightSmall.setPos(-9.0F, -17.0F, 0.0F);
        this.chestRightSmall.addBox(-5.0F, 0.0F, -6.0F, 5, 7, 12, 0.0F);
        this.setRotateAngle(chestRightSmall, 0.08726646259971647F, 0.0F, 0.0F);
        this.strapRight = new ModelPart(this, 0, 56);
        this.strapRight.setPos(-3.0F, -3.5F, -8.0F);
        this.strapRight.addBox(0.0F, -6.0F, -0.5F, 0, 6, 13, 0.0F);
        this.setRotateAngle(strapRight, 0.0F, 0.17453292519943295F, 0.0F);
        this.harness = new ModelPart(this, 0, 69);
        this.harness.setPos(0.0F, -10.5F, 0.0F);
        this.harness.addBox(-9.5F, -13.0F, -13.0F, 19, 26, 26, 0.0F);
        this.eyeRight = new ModelPart(this, 0, 22);
        this.eyeRight.setPos(-3.0F, -3.0F, -10.0F);
        this.eyeRight.addBox(-1.0F, -1.0F, -8.0F, 2, 2, 9, 0.0F);
        this.setRotateAngle(eyeRight, -0.8726646259971648F, 0.4363323129985824F, 0.0F);
        this.chestLeftBig = new ModelPart(this, 124, 80);
        this.chestLeftBig.setPos(9.0F, -7.5F, 0.5F);
        this.chestLeftBig.addBox(0.0F, 0.0F, -8.0F, 7, 10, 16, 0.0F);
        this.setRotateAngle(chestLeftBig, 0.12217304763960307F, 0.0F, 0.0F);
        this.chestFront = new ModelPart(this, 96, 82);
        this.chestFront.setPos(0.0F, -17.0F, -12.5F);
        this.chestFront.addBox(-8.0F, -3.0F, -6.0F, 16, 7, 6, 0.0F);
        this.body2 = new ModelPart(this, 46, 50);
        this.body2.setPos(0.0F, 0.0F, -7.5F);
        this.body2.addBox(-5.0F, -3.5F, -10.0F, 10, 7, 10, 0.0F);
        this.chestBack = new ModelPart(this, 120, 67);
        this.chestBack.setPos(0.0F, -13.5F, 12.5F);
        this.chestBack.addBox(-8.0F, -3.0F, 0.0F, 16, 7, 6, 0.0F);
        this.setRotateAngle(chestBack, 0.06981317007977318F, 0.0F, 0.0F);
        this.head = new ModelPart(this, 88, 0);
        this.head.setPos(0.0F, 0.5F, -10.0F);
        this.head.addBox(-4.0F, -3.0F, -10.0F, 8, 6, 10, 0.0F);
        this.nubLeft = new ModelPart(this, 0, 4);
        this.nubLeft.setPos(3.0F, 1.0F, -10.0F);
        this.nubLeft.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F);
        this.flagPole = new ModelPart(this, 8, 0);
        this.flagPole.setPos(0.0F, -1.5F, -9.0F);
        this.flagPole.addBox(-0.5F, -13.5F, -0.5F, 1, 14, 1, 0.0F);
        this.setRotateAngle(flagPole, 0.17453292519943295F, 0.0F, 0.0F);
        this.seat = new ModelPart(this, 73, 54);
        this.seat.setPos(0.0F, -23.0F, 6.0F);
        this.seat.addBox(-6.0F, -1.5F, -11.0F, 12, 2, 13, 0.0F);
        this.nubRight = new ModelPart(this, 0, 0);
        this.nubRight.setPos(-3.0F, 1.0F, -10.0F);
        this.nubRight.addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2, 0.0F);
        this.body1 = new ModelPart(this, 0, 0);
        this.body1.setPos(0.0F, 20.5F, 0.0F);
        this.body1.addBox(-5.5F, -3.5F, -7.5F, 11, 7, 15, 0.0F);
        this.shell = new ModelPart(this, 27, 0);
        this.shell.setPos(0.0F, -3.5F, 3.0F);
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
    public void renderToBuffer(PoseStack arg0, VertexConsumer arg1, int arg2, int arg3, float arg4, float arg5, float arg6, float arg7) {
        this.body1.render(arg0, arg1, arg2, arg3, arg4, arg5, arg6, opacity);
    }

    @Override
    public void setupAnim(SnailManEntity entity, float f, float g, float h, float i, float j) {
        this.opacity = entity.getOpacity();
    }

    public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
