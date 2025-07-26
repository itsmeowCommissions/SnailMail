package dev.itsmeow.snailmail.block.entity.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;

public interface SnailBoxInterfaceFabric {

    void deserializeHandlerNBT(CompoundTag tag, HolderLookup.Provider provider);

    CompoundTag serializeHandlerNBT(HolderLookup.Provider provider);

    InventoryStorage getItemHandler();

    SimpleContainer getItemContainer();

}
