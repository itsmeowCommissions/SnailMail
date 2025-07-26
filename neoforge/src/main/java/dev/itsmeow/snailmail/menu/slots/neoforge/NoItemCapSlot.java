package dev.itsmeow.snailmail.menu.slots.neoforge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class NoItemCapSlot extends SlotItemHandler {

    public NoItemCapSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getCapability(Capabilities.ItemHandler.ITEM) == null && stack.getItem() != Items.SHULKER_BOX;
    }

}
