package dev.itsmeow.snailmail.init;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import me.shedaniel.architectury.registry.entity.EntityAttributes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ModEntities {

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(SnailMail.MODID, Registry.ENTITY_TYPE_REGISTRY);

    public static RegistrySupplier<EntityType<SnailManEntity>> SNAIL_MAN = ENTITIES.register("snail_man", () -> EntityType.Builder.<SnailManEntity>of(SnailManEntity::new, MobCategory.MISC).sized(2F, 2F).updateInterval(1).clientTrackingRange(64).noSummon().build(SnailMail.MODID + ":snail_man"));

    public static void init() {
        ENTITIES.register();
        EntityAttributes.register(SNAIL_MAN::get, () -> Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 100D).add(Attributes.MOVEMENT_SPEED, 0.25D));
    }
}
