package dev.itsmeow.snailmail.mixin.neoforge;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SnailBoxBlockEntity.class)
public abstract class SnailBoxBlockEntityMixin extends BlockEntity implements dev.itsmeow.snailmail.block.entity.neoforge.SnailBoxInterfaceNeoForge {

    @Unique
    private ItemStackHandler handler = new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
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

    public SnailBoxBlockEntityMixin(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == Capabilities.ItemHandler.ITEM) {
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
    public void deserializeHandlerNBT(CompoundTag nbt, HolderLookup.Provider provider) {
        this.handler.deserializeNBT(provider, nbt);
    }

    @Override
    public CompoundTag serializeHandlerNBT(HolderLookup.Provider provider) {
        return this.handler.serializeNBT(provider);
    }
}
