package dev.itsmeow.snailmail.client;

import com.google.common.collect.ImmutableMap;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.client.model.SnailManModel;
import dev.itsmeow.snailmail.client.screen.EnvelopeScreen;
import dev.itsmeow.snailmail.client.screen.SnailBoxScreen;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.init.ModMenus;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SnailMailClient {

    public static final ResourceLocation TEXTURE = new ResourceLocation(SnailMail.MODID, "textures/entity/snail_man.png");
    public static final ModelLayerLocation SNAILMAN_MODEL = new ModelLayerLocation(new ResourceLocation(SnailMail.MODID, "snail_man"), "main");

    public static void clientInit() {
        MenuRegistry.registerScreenFactory(ModMenus.SNAIL_BOX.get(), SnailBoxScreen::new);
        MenuRegistry.registerScreenFactory(ModMenus.ENVELOPE.get(), EnvelopeScreen::new);
        if(Platform.isFabric()) {
            SnailMailClient.registerEntityRenders();
        }
    }

    public static void registerEntityRenders() {
        EntityRendererRegistry.register(ModEntities.SNAIL_MAN::get, ctx -> new MobRenderer<>(ctx, new SnailManModel(ctx.bakeLayer(SNAILMAN_MODEL)), 0F) {
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

    public static void layerDefinitions(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        builder.put(SNAILMAN_MODEL, SnailManModel.createBodyLayer());
    }

}
