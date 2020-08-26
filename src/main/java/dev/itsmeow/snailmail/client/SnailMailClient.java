package dev.itsmeow.snailmail.client;

import dev.itsmeow.imdlib.client.render.RenderFactory;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.model.SnailManModel;
import dev.itsmeow.snailmail.client.screen.EnvelopeScreen;
import dev.itsmeow.snailmail.client.screen.SnailBoxScreen;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import dev.itsmeow.snailmail.init.ModContainers;
import dev.itsmeow.snailmail.init.ModEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SnailMail.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SnailMailClient {

    public static final ResourceLocation TEXTURE = new ResourceLocation(SnailMail.MODID, "textures/entity/snail_man.png");

    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainers.SNAIL_BOX, SnailBoxScreen::new);
        ScreenManager.registerFactory(ModContainers.ENVELOPE, EnvelopeScreen::new);
        RenderFactory.addRender(ModEntities.SNAIL_MAN.entityType, mgr -> new LivingRenderer<SnailManEntity, SnailManModel>(mgr, new SnailManModel(), 0F) {

            @Override
            protected RenderType getRenderLayer(SnailManEntity entity, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
                return RenderType.getEntityTranslucent(this.getEntityTexture(entity), true);
            }

            @Override
            public ResourceLocation getEntityTexture(SnailManEntity entity) {
                return TEXTURE;
            }

            @Override
            protected boolean canRenderName(SnailManEntity entity) {
                return false;
            }
        });
    }

}
