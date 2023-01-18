package net.examplemod.init;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.examplemod.SnailMail;
import net.examplemod.block.entity.SnailBoxBlockEntity;
import net.examplemod.item.EnvelopeItem;
import net.examplemod.menu.EnvelopeMenu;
import net.examplemod.menu.SnailBoxMenu;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(SnailMail.MODID, Registry.MENU_REGISTRY);

    public static RegistrySupplier<MenuType<SnailBoxMenu>> SNAIL_BOX = r("snail_box", () -> MenuRegistry.ofExtended(SnailBoxBlockEntity::getClientMenu));
    public static RegistrySupplier<MenuType<EnvelopeMenu>> ENVELOPE = r("envelope", () -> MenuRegistry.ofExtended(EnvelopeItem::getClientMenu));

    private static <T extends AbstractContainerMenu> RegistrySupplier<MenuType<T>> r(String name, Supplier<MenuType<T>> b) {
        return MENUS.register(name, b);
    }

    public static void init() {
        MENUS.register();
    }
}
