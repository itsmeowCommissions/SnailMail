package dev.itsmeow.snailmail.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.SnailMailClient;
import dev.itsmeow.snailmail.util.forge.SnailMailCommonConfigImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

@Mod(value = SnailMail.MODID)
public class SnailMailForge {

    public SnailMailForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(SnailMail.MODID, modBus);
        SnailMail.construct();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SnailMailCommonConfigImpl.Configuration.initSpec());
        if(Platform.getEnv() == Dist.CLIENT) {
            Supplier<Runnable> target = () -> SnailMailClient::registerEntityRenders;
            target.get().run();
        }
    }

}
