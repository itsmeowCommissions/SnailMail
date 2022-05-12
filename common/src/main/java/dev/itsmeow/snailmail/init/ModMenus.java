package dev.itsmeow.snailmail.init;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.MenuRegistry;
import me.shedaniel.architectury.registry.RegistrySupplier;
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
