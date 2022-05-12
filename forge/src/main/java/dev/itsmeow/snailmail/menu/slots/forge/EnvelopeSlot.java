package dev.itsmeow.snailmail.menu.slots.forge;

import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class EnvelopeSlot extends SlotItemHandler {

    public EnvelopeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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
