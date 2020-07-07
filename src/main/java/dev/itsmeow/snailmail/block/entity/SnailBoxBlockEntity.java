package dev.itsmeow.snailmail.block.entity;

import java.util.UUID;

import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SnailBoxBlockEntity extends TileEntity {

    private static final int SLOT_COUNT = 27;
    private ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
        }
    };
    public final LazyOptional<ItemStackHandler> handlerOptional = LazyOptional.of(() -> handler);
    public static final ITextComponent TITLE = new TranslationTextComponent("container.snailmail.snail_box");
    private static final String ITEM_TAG_KEY = "item_handler";
    private UUID owner;

    public SnailBoxBlockEntity() {
        super(ModBlockEntities.SNAIL_BOX);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        if(owner != null) {
            SnailBoxData.getData(this.getWorld().getServer()).removeBox(owner, this.getPos());
        }
        this.owner = uuid;
        SnailBoxData.getData(this.getWorld().getServer()).addBox(owner, this.getPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(owner != null) {
            SnailBoxData.getData(this.getWorld().getServer()).addBox(owner, this.getPos());
        }
    }

    public ItemStackHandler getItemHandler() {
        return handler;
    }

    @Override
    public void remove() {
        this.handlerOptional.invalidate();
        super.remove();
    }

    @Override
    public void updateContainingBlockInfo() {
        // remove if air
        if(owner != null && world.isBlockPresent(this.getPos()) && world.getBlockState(this.getPos()).getBlock() != ModBlocks.SNAIL_BOX) {
            SnailBoxData.getData(this.getWorld().getServer()).removeBox(owner, this.getPos());
        }
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.handler.deserializeNBT(compound.getCompound(ITEM_TAG_KEY));
        this.owner = compound.getUniqueId("OwnerUUID");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put(ITEM_TAG_KEY, this.handler.serializeNBT());
        if(owner != null) {
            compound.putUniqueId("OwnerUUID", owner);
        }
        return compound;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    public void openGUI(ServerPlayerEntity player) {
        IContainerProvider provider = SnailBoxContainer.getServerContainerProvider(this);
        INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(provider, TITLE);
        NetworkHooks.openGui(player, namedProvider, pos);
    }

    public static class SnailBoxContainer extends Container {

        public static SnailBoxContainer getClientContainer(int id, PlayerInventory playerInventory) {
            return new SnailBoxContainer(id, playerInventory, BlockPos.ZERO, new ItemStackHandler(SLOT_COUNT));
        }

        public static IContainerProvider getServerContainerProvider(SnailBoxBlockEntity te) {
            return (id, playerInventory, serverPlayer) -> new SnailBoxContainer(id, playerInventory, te.getPos(), te.getItemHandler());
        }

        private final BlockPos pos;
        private final ItemStackHandler items;

        public SnailBoxContainer(int id, IInventory playerInventory, BlockPos pos, ItemStackHandler items) {
            super(ModContainers.SNAIL_BOX, id);
            this.items = items;
            this.pos = pos;
            addOwnSlots();
            addPlayerSlots(playerInventory);
        }

        private void addPlayerSlots(IInventory playerInventory) {
            // Slots for the main inventory
            for(int row = 0; row < 3; ++row) {
                for(int col = 0; col < 9; ++col) {
                    int x = 8 + col * 18;
                    int y = row * 18 + 89;
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, x, y));
                }
            }

            // Slots for the hotbar
            for(int row = 0; row < 9; ++row) {
                int x = 8 + row * 18;
                int y = 147;
                this.addSlot(new Slot(playerInventory, row, x, y));
            }
        }

        private void addOwnSlots() {
            for(int i = 0; i < items.getSlots(); i++) {
                int yCoord = i / 9 * 18; // 9 slots fit per row, 18 is size of the slot texture
                int xCoord = i % 9 * 18; // 0, 1*18, 2*18, 3*18, loop per row
                this.addSlot(new SlotItemHandler(items, i, 8 + xCoord, 19 + yCoord));
            }
        }

        @Override
        public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.inventorySlots.get(index);

            if(slot != null && slot.getHasStack()) {
                ItemStack itemstack1 = slot.getStack();
                itemstack = itemstack1.copy();

                if(index < SLOT_COUNT) {
                    if(!this.mergeItemStack(itemstack1, SLOT_COUNT, this.inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if(!this.mergeItemStack(itemstack1, 0, SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }

                if(itemstack1.isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
            }

            return itemstack;
        }

        @Override
        public boolean canInteractWith(PlayerEntity player) {
            return isWithinUsableDistance(IWorldPosCallable.of(player.world, pos), player, ModBlocks.SNAIL_BOX);
        }
    }
}
