package dev.itsmeow.snailmail.neoforge;

import dev.architectury.platform.Platform;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.SnailMailClient;
import dev.itsmeow.snailmail.util.neoforge.SnailMailCommonConfigImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import java.util.function.Supplier;

@Mod(value = SnailMail.MODID)
public class SnailMailNeoForge {

    public SnailMailNeoForge(ModContainer context, IEventBus modBus) {
        SnailMail.construct();
        context.registerConfig(ModConfig.Type.COMMON, SnailMailCommonConfigImpl.Configuration.initSpec());
        if(Platform.getEnv() == Dist.CLIENT) {
            Supplier<Runnable> target = () -> SnailMailClient::registerEntityRenders;
            target.get().run();
        }
    }

}
