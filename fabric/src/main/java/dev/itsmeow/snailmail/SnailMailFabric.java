package dev.itsmeow.snailmail;

import dev.itsmeow.snailmail.block.entity.fabric.SnailBoxInterfaceFabric;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public class SnailMailFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SnailMail.construct();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> ((SnailBoxInterfaceFabric) blockEntity).getItemHandler(), ModBlockEntities.SNAIL_BOX.get());
    }
}
