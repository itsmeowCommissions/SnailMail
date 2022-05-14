package dev.itsmeow.snailmail.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SnailManModel extends EntityModel<SnailManEntity> {

    public ModelPart body1;
    public ModelPart shell;
    public ModelPart harness;
    public ModelPart chestLeftSmall;
    public ModelPart chestLeftBig;
    public ModelPart chestRightSmall;
    public ModelPart chestRightBig;
    public ModelPart chestBack;
    public ModelPart chestFront;
    public ModelPart seat;
    public ModelPart flagPole;
    public ModelPart flag;
    public ModelPart tail;
    public ModelPart body2;
    public ModelPart head;
    public ModelPart eyeLeft;
    public ModelPart eyeRight;
    public ModelPart nubRight;
    public ModelPart nubLeft;
    public ModelPart strapLeft;
    public ModelPart strapRight;
    private float opacity;

    public SnailManModel(ModelPart root) {
        this.body1 = root.getChild("body1");
        this.shell = body1.getChild("shell");
        this.harness = shell.getChild("harness");
        this.chestLeftSmall = shell.getChild("chestLeftSmall");
        this.chestLeftBig = shell.getChild("chestLeftBig");
        this.chestRightSmall = shell.getChild("chestRightSmall");
        this.chestRightBig = shell.getChild("chestRightBig");
        this.chestBack = shell.getChild("chestBack");
        this.chestFront = shell.getChild("chestFront");
        this.seat = shell.getChild("seat");
        this.flagPole = seat.getChild("flagPole");
        this.flag = flagPole.getChild("flag");
        this.tail = body1.getChild("tail");
        this.body2 = body1.getChild("body2");
        this.head = body2.getChild("head");
        this.eyeLeft = head.getChild("eyeLeft");
        this.eyeRight = head.getChild("eyeRight");
        this.nubRight = head.getChild("nubRight");
        this.nubLeft = head.getChild("nubLeft");
        this.strapLeft = body2.getChild("strapLeft");
        this.strapRight = body2.getChild("strapRight");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body1 = partdefinition.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -3.5F, -7.5F, 11.0F, 7.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.5F, 0.0F));
        PartDefinition shell = body1.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(27, 0).addBox(-9.0F, -23.0F, -12.5F, 18.0F, 25.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 3.0F, -0.1222F, 0.0F, 0.0F));
        PartDefinition harness = shell.addOrReplaceChild("harness", CubeListBuilder.create().texOffs(0, 69).addBox(-9.5F, -13.0F, -13.0F, 19.0F, 26.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.5F, 0.0F));
        PartDefinition chestLeftSmall = shell.addOrReplaceChild("chestLeftSmall", CubeListBuilder.create().texOffs(132, 22).mirror().addBox(-5.0F, 0.0F, -6.0F, 5.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.0F, -17.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
        PartDefinition chestLeftBig = shell.addOrReplaceChild("chestLeftBig", CubeListBuilder.create().texOffs(124, 80).mirror().addBox(-7.0F, 0.0F, -8.0F, 7.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-9.0F, -7.5F, 0.5F, 0.1222F, 0.0F, 0.0F));
        PartDefinition chestRightSmall = shell.addOrReplaceChild("chestRightSmall", CubeListBuilder.create().texOffs(110, 38).mirror().addBox(0.0F, 0.0F, -6.0F, 5.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.0F, -17.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
        PartDefinition chestRightBig = shell.addOrReplaceChild("chestRightBig", CubeListBuilder.create().texOffs(128, 41).mirror().addBox(0.0F, 0.0F, -8.0F, 7.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(9.0F, -7.5F, 0.5F, 0.1222F, 0.0F, 0.0F));
        PartDefinition chestBack = shell.addOrReplaceChild("chestBack", CubeListBuilder.create().texOffs(120, 67).addBox(-8.0F, -3.0F, 0.0F, 16.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.5F, 12.5F, 0.0698F, 0.0F, 0.0F));
        PartDefinition seat = shell.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(73, 54).addBox(-6.0F, -1.5F, -11.0F, 12.0F, 2.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -23.0F, 6.0F));
        PartDefinition flagPole = seat.addOrReplaceChild("flagPole", CubeListBuilder.create().texOffs(8, 0).addBox(-0.5F, -13.5F, -0.5F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.5F, -9.0F, 0.1745F, 0.0F, 0.0F));
        PartDefinition flag = flagPole.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(64, 51).addBox(0.0F, -5.0F, 0.0F, 0.0F, 10.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, 0.5F));
        PartDefinition chestFront = shell.addOrReplaceChild("chestFront", CubeListBuilder.create().texOffs(96, 82).addBox(-8.0F, -3.0F, -6.0F, 16.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, -12.5F));
        PartDefinition tail = body1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 50).addBox(-4.5F, -2.5F, 0.0F, 9.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 7.5F));
        PartDefinition body2 = body1.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(46, 50).addBox(-5.0F, -3.5F, -10.0F, 10.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.5F));
        PartDefinition head = body2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(88, 0).addBox(-4.0F, -3.0F, -10.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, -10.0F));
        PartDefinition eyeLeft = head.addOrReplaceChild("eyeLeft", CubeListBuilder.create().texOffs(0, 33).addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.0F, -10.0F, -0.8727F, 0.4363F, 0.0F));
        PartDefinition eyeRight = head.addOrReplaceChild("eyeRight", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.0F, -10.0F, -0.8727F, -0.4363F, 0.0F));
        PartDefinition nubRight = head.addOrReplaceChild("nubRight", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 1.0F, -10.0F));
        PartDefinition nubLeft = head.addOrReplaceChild("nubLeft", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 1.0F, -10.0F));
        PartDefinition strapLeft = body2.addOrReplaceChild("strapLeft", CubeListBuilder.create().texOffs(0, 56).addBox(0.0F, -6.0F, -0.5F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.5F, -8.0F, 0.0F, 0.1745F, 0.0F));
        PartDefinition strapRight = body2.addOrReplaceChild("strapRight", CubeListBuilder.create().texOffs(0, 56).addBox(0.0F, -6.0F, -0.5F, 0.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.5F, -8.0F, 0.0F, -0.1745F, 0.0F));
        return LayerDefinition.create(meshdefinition, 180, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, opacity);
    }

    @Override
    public void setupAnim(SnailManEntity entity, float f, float g, float h, float i, float j) {
        this.opacity = entity.getOpacity();
    }

}
