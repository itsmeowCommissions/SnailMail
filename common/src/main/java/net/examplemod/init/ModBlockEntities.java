package net.examplemod.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.examplemod.SnailMail;
import net.examplemod.block.entity.SnailBoxBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(SnailMail.MODID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    public static RegistrySupplier<BlockEntityType<SnailBoxBlockEntity>> SNAIL_BOX = r("snail_box", () -> BlockEntityType.Builder.of(SnailBoxBlockEntity::new, ModBlocks.SNAIL_BOX.get()).build(null));

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> r(String name, Supplier<BlockEntityType<T>> b) {
        return BLOCK_ENTITIES.register(name, b);
    }

    public static void init() {
        BLOCK_ENTITIES.register();
    }
}
