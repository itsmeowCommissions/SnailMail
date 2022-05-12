package dev.itsmeow.snailmail.menu.slots;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStorageSlot extends Slot {

    private static Container emptyInventory = new SimpleContainer(0);
    private final InventoryStorage itemHandler;
    private final int index;

    public ItemStorageSlot(InventoryStorage itemHandler, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return itemHandler.getSlot(index).supportsInsertion();
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return this.itemHandler.getSlot(index).getResource().toStack((int) this.itemHandler.getSlot(index).getAmount());
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        try(Transaction transaction = Transaction.openOuter()) {
            itemHandler.getSlot(index).extract(itemHandler.getSlot(index).getResource(), itemHandler.getSlot(index).getAmount(), transaction);
            if(!stack.isEmpty()) {
                itemHandler.getSlot(index).insert(ItemVariant.of(stack), stack.getCount(), transaction);
            }
            transaction.commit();
        }
        this.setChanged();
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {

    }

    @Override
    public int getMaxStackSize() {
        return (int) this.itemHandler.getSlot(this.index).getCapacity();
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        ItemStack maxAdd = stack.copy();
        int maxInput = stack.getMaxStackSize();
        ItemStack currentStack = this.getItem();
        this.set(ItemStack.EMPTY);
        int insertable = (int) itemHandler.simulateInsert(ItemVariant.of(maxAdd), maxInput, null);
        this.set(currentStack);
        return insertable;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !this.itemHandler.getSlot(index).isResourceBlank();
    }

    @Override
    @NotNull
    public ItemStack remove(int amount) {
        try(Transaction transaction = Transaction.openOuter()) {
            Item resource = this.itemHandler.getSlot(index).getResource().getItem();
            int removed = (int) this.itemHandler.getSlot(index).extract(this.itemHandler.getSlot(index).getResource(), amount, transaction);
            transaction.commit();
            return amount - removed > 0 ? new ItemStack(resource, amount - removed) : ItemStack.EMPTY;
        }
    }

}
