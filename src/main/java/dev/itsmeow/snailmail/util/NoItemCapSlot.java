package dev.itsmeow.snailmail.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class NoItemCapSlot extends SlotItemHandler {

    public NoItemCapSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent() && stack.getItem() != Items.SHULKER_BOX;
    }

}
