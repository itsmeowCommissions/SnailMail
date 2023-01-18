package net.examplemod;

import net.examplemod.block.entity.fabric.SnailBoxInterfaceFabric;
import net.examplemod.init.ModBlockEntities;
import net.examplemod.util.fabric.SnailMailCommonConfigImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SnailMailFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SnailMailFabricLike.init();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> ((SnailBoxInterfaceFabric) blockEntity).getItemHandler(), ModBlockEntities.SNAIL_BOX.get());
        ModLoadingContext.registerConfig(SnailMail.MODID, ModConfig.Type.COMMON, SnailMailCommonConfigImpl.Configuration.initSpec());
    }
}
