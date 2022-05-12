package dev.itsmeow.snailmail.forge;

import dev.itsmeow.snailmail.SnailMail;
import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = SnailMail.MODID)
public class SnailMailForge {

    public SnailMailForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(SnailMail.MODID, modBus);
        SnailMail.construct();
    }

}
