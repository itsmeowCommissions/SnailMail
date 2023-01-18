package dev.itsmeow.snailmail.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SnailMail.MODID, Registries.ITEM);

    public static RegistrySupplier<BlockItem> SNAIL_BOX = rIB(ModBlocks.SNAIL_BOX);
    public static RegistrySupplier<EnvelopeItem> ENVELOPE_OPEN = r("envelope_open", () -> new EnvelopeItem(true));
    public static RegistrySupplier<EnvelopeItem> ENVELOPE_CLOSED = r("envelope_closed", () -> new EnvelopeItem(false));
    public static RegistrySupplier<Item> STAMP = r("stamp", () -> new Item(new Item.Properties().arch$tab(SnailMail.ITEM_GROUP)));

    protected static <T extends Item> RegistrySupplier<T> r(String name, Supplier<T> b) {
        return ITEMS.register(name, b);
    }

    protected static RegistrySupplier<BlockItem> rIB(RegistrySupplier<? extends Block> parent) {
        return ITEMS.register(parent.getId().getPath(), () -> new BlockItem(parent.get(), new Item.Properties().arch$tab(SnailMail.ITEM_GROUP)));
    }

    public static void init() {
        ITEMS.register();
    }
}
