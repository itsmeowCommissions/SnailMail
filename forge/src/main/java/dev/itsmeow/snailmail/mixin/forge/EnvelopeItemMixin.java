package dev.itsmeow.snailmail.mixin.forge;

import dev.itsmeow.snailmail.item.EnvelopeItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnvelopeItem.class)
public abstract class EnvelopeItemMixin extends Item {

    @Shadow(remap = false)
    @Final
    private boolean isOpen;

    public EnvelopeItemMixin(Properties arg) {
        super(arg);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new EnvelopeCapabilityProvider(stack, nbt, isOpen);
    }

    public static class EnvelopeCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

        private ItemStackHandler handler;
        public final LazyOptional<ItemStackHandler> handlerOptional;
        private ItemStack stack;

        public EnvelopeCapabilityProvider(ItemStack stack, CompoundTag compound, boolean isOpen) {
            this.handler = new ItemStackHandler(isOpen ? 28 : 27);
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
}
