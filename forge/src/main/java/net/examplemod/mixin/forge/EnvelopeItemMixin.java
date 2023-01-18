package net.examplemod.mixin.forge;

import net.examplemod.util.forge.EnvelopeCapabilityProvider;
import net.examplemod.item.EnvelopeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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
}
