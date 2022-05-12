package dev.itsmeow.snailmail.init;

import dev.itsmeow.imdlib.IMDLib;
import dev.itsmeow.imdlib.entity.EntityRegistrarHandler;
import dev.itsmeow.imdlib.entity.EntityTypeContainer;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ModEntities {

    public static final EntityRegistrarHandler H = IMDLib.entityHandler(SnailMail.MODID);

    public static final EntityTypeContainer<SnailManEntity> SNAIL_MAN = H.add(SnailManEntity.class, SnailManEntity::new, "snail_man", () -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100D).add(Attributes.MOVEMENT_SPEED, 0.25D), b -> b.size(2F, 2F));

    public static void init() {
        H.init();
    }
}
