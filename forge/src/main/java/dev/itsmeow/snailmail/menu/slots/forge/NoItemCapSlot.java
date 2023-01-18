package dev.itsmeow.snailmail.menu.slots.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class NoItemCapSlot extends SlotItemHandler {

    public NoItemCapSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent() && stack.getItem() != Items.SHULKER_BOX;
    }

}
