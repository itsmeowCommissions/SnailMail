package net.examplemod.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import net.examplemod.SnailMail;
import net.examplemod.client.SnailMailClient;
import net.examplemod.util.forge.SnailMailCommonConfigImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

@Mod(SnailMail.MODID)
public class SnailMailForge {
    public SnailMailForge() {
        EventBuses.registerModEventBus(SnailMail.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        SnailMail.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SnailMailCommonConfigImpl.Configuration.initSpec());
        if(Platform.getEnv() == Dist.CLIENT) {
            Supplier<Runnable> target = () -> SnailMailClient::registerEntityRenders;
            target.get().run();
        }
    }
}
