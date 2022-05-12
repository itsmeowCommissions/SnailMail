package dev.itsmeow.snailmail.mixin.forge;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.block.entity.forge.SnailBoxInterfaceForge;
import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SnailBoxBlockEntity.class)
public abstract class SnailBoxBlockEntityMixin extends BlockEntity implements SnailBoxInterfaceForge {

    @Unique
    private ItemStackHandler handler = new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == 27) {
                return stack.getItem() == ModItems.ENVELOPE_OPEN.get();
            } else {
                return stack.getItem() == ModItems.ENVELOPE_CLOSED.get();
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };;
    @Unique
    private LazyOptional<ItemStackHandler> handlerOptional = LazyOptional.of(() -> handler);

    public SnailBoxBlockEntityMixin(BlockEntityType<?> arg) {
        super(arg);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void handleRemoved() {
        this.handlerOptional.invalidate();
    }

    @Override
    public ItemStackHandler getItemHandler() {
        return handler;
    }

    @Override
    public void deserializeHandlerNBT(CompoundTag nbt) {
        this.handler.deserializeNBT(nbt);
    }

    @Override
    public CompoundTag serializeHandlerNBT() {
        return this.handler.serializeNBT();
    }
}