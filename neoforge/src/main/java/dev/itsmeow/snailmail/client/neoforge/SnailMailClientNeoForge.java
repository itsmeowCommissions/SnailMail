package dev.itsmeow.snailmail.client.neoforge;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.SnailMailClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = SnailMail.MODID, value = Dist.CLIENT)
public class SnailMailClientNeoForge {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        SnailMailClient.clientInit();
    }
}
