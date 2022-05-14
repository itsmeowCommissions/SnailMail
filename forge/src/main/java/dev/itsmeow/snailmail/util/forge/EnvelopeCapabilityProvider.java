package dev.itsmeow.snailmail.util.forge;

import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class EnvelopeCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

    private ItemStackHandler handler;
    public final LazyOptional<ItemStackHandler> handlerOptional;
    private ItemStack stack;

    public EnvelopeCapabilityProvider(ItemStack stack, CompoundTag compound, boolean isOpen) {
        this.handler = new ItemStackHandler(isOpen ? 28 : 27) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if(slot == 27) {
                    return stack.getItem() == ModItems.STAMP.get();
                }
                return !stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent() && stack.getItem() != Items.SHULKER_BOX;
            }
        };
        this.handlerOptional = LazyOptional.of(() -> handler);
        this.stack = stack;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction dir) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !stack.isEmpty()) {
            return handlerOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt != null) {
            handler.deserializeNBT(nbt);
        }
    }

}