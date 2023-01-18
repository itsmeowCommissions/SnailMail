package net.examplemod.menu;

import net.examplemod.block.SnailBoxBlock;
import net.examplemod.block.entity.SnailBoxBlockEntity;
import net.examplemod.init.ModBlocks;
import net.examplemod.init.ModItems;
import net.examplemod.init.ModMenus;
import net.examplemod.item.EnvelopeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashSet;
import java.util.Set;

public abstract class SnailBoxMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public String startingName;
    public boolean isOwner = false;
    public boolean isPublic = false;
    public Set<String> memberUsers = new HashSet<String>();

    public SnailBoxMenu(int id, Container playerInventory, BlockPos pos) {
        super(ModMenus.SNAIL_BOX.get(), id);
        this.pos = pos;
        this.startingName = "";
    }

    public SnailBoxMenu(int id, Container playerInventory, BlockPos pos, String startingName, boolean isOwner, boolean isPublic, Set<String> members) {
        super(ModMenus.SNAIL_BOX.get(), id);
        this.pos = pos;
        this.startingName = startingName;
        this.isOwner = isOwner;
        this.isPublic = isPublic;
        this.memberUsers = members;
    }

    protected void addPlayerSlots(Container playerInventory) {
        // Slots for the main inventory
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 117;
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Slots for the hotbar
        for(int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 175;
            this.addSlot(new Slot(playerInventory, row, x, y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if(index < SnailBoxBlockEntity.SLOT_COUNT) {
                if(!this.moveItemStackTo(itemstack1, SnailBoxBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(!this.moveItemStackTo(itemstack1, 0, SnailBoxBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if(itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level, pos), player, ModBlocks.SNAIL_BOX.get()) && SnailBoxBlock.canOpen(player.level, pos, player);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        // the player might have filled the envelope then moved it to their inventory - in this case, dump out all the items in the envelope
        if (!playerIn.level.isClientSide()) {
            playerIn.getServer().execute(() -> {
                for (int i = 0; i < playerIn.getInventory().getContainerSize(); i++) {
                    ItemStack stack = playerIn.getInventory().getItem(i);
                    if (stack.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                        EnvelopeItem.emptyEnvelope(stack, playerIn);
                        playerIn.getInventory().setItem(i, new ItemStack(ModItems.ENVELOPE_OPEN.get()));
                    }
                }
            });
        }
    }

    public SnailBoxBlockEntity getTile(Player player) {
        BlockEntity te = player.level.getBlockEntity(pos);
        if(te != null && stillValid(player) && player.level.getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX.get() && te instanceof SnailBoxBlockEntity) {
            return ((SnailBoxBlockEntity) te);
        }
        return null;
    }
}
