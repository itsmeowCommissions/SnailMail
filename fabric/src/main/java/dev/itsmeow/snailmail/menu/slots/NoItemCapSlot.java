package dev.itsmeow.snailmail.menu.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NoItemCapSlot extends Slot {

    public NoItemCapSlot(SimpleContainer itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() != Items.SHULKER_BOX;
    }

}
