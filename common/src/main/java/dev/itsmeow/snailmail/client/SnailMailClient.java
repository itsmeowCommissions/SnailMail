package dev.itsmeow.snailmail.client;

import dev.itsmeow.imdlib.client.render.RenderFactory;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.model.SnailManModel;
import dev.itsmeow.snailmail.client.screen.EnvelopeScreen;
import dev.itsmeow.snailmail.client.screen.SnailBoxScreen;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.init.ModMenus;
import me.shedaniel.architectury.registry.MenuRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SnailMailClient {

    public static final ResourceLocation TEXTURE = new ResourceLocation(SnailMail.MODID, "textures/entity/snail_man.png");

    public static void clientInit() {
        MenuRegistry.registerScreenFactory(ModMenus.SNAIL_BOX.get(), SnailBoxScreen::new);
        MenuRegistry.registerScreenFactory(ModMenus.ENVELOPE.get(), EnvelopeScreen::new);
        RenderFactory.addRender(ModEntities.SNAIL_MAN.getEntityType(), mgr -> new MobRenderer<SnailManEntity, SnailManModel>(mgr, new SnailManModel(), 0F) {
            @Override
            public ResourceLocation getTextureLocation(SnailManEntity entity) {
                return TEXTURE;
            }

            @Override
            protected RenderType getRenderType(SnailManEntity livingEntity, boolean bl, boolean bl2, boolean bl3) {
                return RenderType.entityTranslucent(this.getTextureLocation(livingEntity), true);
            }

            @Override
            protected boolean shouldShowName(SnailManEntity mob) {
                return false;
            }
        });
    }

}
