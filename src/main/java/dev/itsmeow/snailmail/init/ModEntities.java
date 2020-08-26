package dev.itsmeow.snailmail.init;

import dev.itsmeow.imdlib.IMDLib;
import dev.itsmeow.imdlib.entity.EntityRegistrarHandler;
import dev.itsmeow.imdlib.entity.util.EntityTypeContainer;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;

public class ModEntities {

    public static final EntityRegistrarHandler H = IMDLib.entityHandler(SnailMail.MODID);

    public static final EntityTypeContainer<SnailManEntity> SNAIL_MAN = H.add(EntityTypeContainer.Builder.<SnailManEntity>create(SnailManEntity.class, SnailManEntity::new, "snail_man", () -> MobEntity.createMobAttributes().add(Attributes.GENERIC_MOVEMENT_SPEED, 0.25D), SnailMail.MODID).size(2F, 2F));

}
