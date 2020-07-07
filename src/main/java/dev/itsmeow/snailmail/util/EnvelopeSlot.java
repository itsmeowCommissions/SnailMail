package dev.itsmeow.snailmail.util;

import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class EnvelopeSlot extends SlotItemHandler {

    public EnvelopeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getSlotStackLimit();
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() == ModItems.ENVELOPE_OPEN;
    }

}
