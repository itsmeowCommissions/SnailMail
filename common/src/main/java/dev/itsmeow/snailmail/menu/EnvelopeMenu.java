package dev.itsmeow.snailmail.menu;

import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class EnvelopeMenu extends AbstractContainerMenu {

    private final int SLOT_COUNT;
    public final String clientStartToName;
    public final String clientStartFromName;
    public final BlockPos returnPos;

    public EnvelopeMenu(int id, Container playerInventory, int slotCount, BlockPos returnPos, String toName, String fromName) {
        super(ModMenus.ENVELOPE.get(), id);
        this.returnPos = returnPos;
        this.clientStartToName = toName;
        this.clientStartFromName = fromName;
        this.SLOT_COUNT = slotCount;
    }

    protected void addPlayerSlots(Container playerInventory) {
        // Slots for the main inventory
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 96;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Slots for the hotbar
        for(int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 154;
            this.addSlot(new Slot(playerInventory, row, x, y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < this.SLOT_COUNT) {
                if (!this.moveItemStackTo(itemStack2, this.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, this.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level.isClientSide()) {
            player.getServer().execute(() -> {
                BlockPos pos = returnPos;
                BlockEntity blockEntity = player.level.getBlockEntity(pos);
                if(!(blockEntity instanceof SnailBoxBlockEntity)) {
                    pos = SnailBoxBlock.lastClickedBox.get(player.getUUID());
                    blockEntity = player.level.getBlockEntity(pos);
                }
                if (blockEntity instanceof SnailBoxBlockEntity){
                    if (SnailBoxBlock.canOpen(player.level, pos, player)){
                        ((SnailBoxBlockEntity) player.level.getBlockEntity(pos)).openGUI((ServerPlayer) player);
                    }
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
