package dev.itsmeow.snailmail.menu.slots.neoforge;

import dev.itsmeow.snailmail.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class StampSlot extends SlotItemHandler {

    public StampSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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
        return stack.getItem() == ModItems.STAMP.get();
    }

}
