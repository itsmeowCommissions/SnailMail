package dev.itsmeow.snailmail.block.entity.forge;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;

public interface SnailBoxInterfaceForge {

    void handleRemoved();

    void deserializeHandlerNBT(CompoundTag tag);

    CompoundTag serializeHandlerNBT();

    ItemStackHandler getItemHandler();
}
