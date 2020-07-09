package dev.itsmeow.snailmail.client;

import dev.itsmeow.imdlib.client.IMDLibClient;
import dev.itsmeow.imdlib.client.render.RenderFactory;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.model.SnailManModel;
import dev.itsmeow.snailmail.client.screen.EnvelopeScreen;
import dev.itsmeow.snailmail.client.screen.SnailBoxScreen;
import dev.itsmeow.snailmail.init.ModContainers;
import dev.itsmeow.snailmail.init.ModEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SnailMail.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SnailMailClient {

    public static final RenderFactory H = IMDLibClient.getRenderRegistry(SnailMail.MODID);

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainers.SNAIL_BOX, SnailBoxScreen::new);
        ScreenManager.registerFactory(ModContainers.ENVELOPE, EnvelopeScreen::new);
        H.addRender(ModEntities.SNAIL_MAN.entityType, 1F, r -> r.tSingle("snail_man").mSingle(new SnailManModel()));
    }

}
