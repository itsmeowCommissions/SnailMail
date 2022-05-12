package dev.itsmeow.snailmail.client.forge;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.SnailMailClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SnailMail.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SnailMailClientForge {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        SnailMailClient.clientInit();
    }
}
