package dev.itsmeow.snailmail.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.SnailBoxBlock;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ModBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(SnailMail.MODID, Registry.BLOCK_REGISTRY);

    public static RegistrySupplier<SnailBoxBlock> SNAIL_BOX = r("snail_box", SnailBoxBlock::new);

    private static <T extends Block> RegistrySupplier<T> r(String name, Supplier<T> b) {
        return BLOCKS.register(name, b);
    }

    public static void init() {
        BLOCKS.register();
    }
}
