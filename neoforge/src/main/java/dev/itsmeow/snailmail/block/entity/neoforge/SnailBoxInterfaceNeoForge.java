package dev.itsmeow.snailmail.block.entity.neoforge;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.items.ItemStackHandler;

public interface SnailBoxInterfaceNeoForge {

    void handleRemoved();

    void deserializeHandlerNBT(CompoundTag tag, HolderLookup.Provider provider);

    CompoundTag serializeHandlerNBT(HolderLookup.Provider provider);

    ItemStackHandler getItemHandler();
}
