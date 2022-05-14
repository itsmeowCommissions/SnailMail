package dev.itsmeow.snailmail.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMailFabric;
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import me.shedaniel.fiber2cloth.api.Fiber2Cloth;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Supplier;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        Supplier<Supplier<ConfigScreenFactory<?>>> supplier = () -> ClothConfigScreenFactory::new;
        return FabricLoader.getInstance().isModLoaded("cloth-config") ? supplier.get().get() : screen -> null;
    }

    public static class ClothConfigScreenFactory implements ConfigScreenFactory<Screen> {

        public ClothConfigScreenFactory() {}

        @Override
        public Screen create(Screen parent) {
            ConfigTreeBuilder b = ConfigTree.builder();
            for (SnailMailFabric.FabricCommonConfig c : SnailMailFabric.FabricCommonConfig.INSTANCES) {
                c.getBranch().detach();
                b.withChild(c.getBranch());
            }
            Fiber2Cloth fiber2Cloth = Fiber2Cloth.create(parent, SnailMail.MODID, b.build(), SnailMail.MODID).setTitleText(new TranslatableComponent("config." + SnailMail.MODID)).setSaveRunnable(() -> {
                for (SnailMailFabric.FabricCommonConfig c : SnailMailFabric.FabricCommonConfig.INSTANCES) {
                    c.saveBranch(c.getConfigFile(), b.lookupBranch(c.getConfigName()));
                }
            });
            return fiber2Cloth.build().getScreen();
        }
    }
}
