package net.examplemod.menu.slots;

import net.examplemod.init.ModItems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EnvelopeSlot extends Slot {
    public EnvelopeSlot(SimpleContainer itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getMaxStackSize();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() == ModItems.ENVELOPE_OPEN.get();
    }
}
