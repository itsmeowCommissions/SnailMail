package dev.itsmeow.snailmail.block.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModContainers;
import dev.itsmeow.snailmail.util.EnvelopeSlot;
import dev.itsmeow.snailmail.util.Location;
import dev.itsmeow.snailmail.util.ReadOnlySlot;
import dev.itsmeow.snailmail.util.ServerBoxData;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SnailBoxBlockEntity extends TileEntity {

    private static final int SLOT_COUNT = 28;
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
    private ServerBoxData boxData;

    public SnailBoxBlockEntity() {
        super(ModBlockEntities.SNAIL_BOX);
    }

    public boolean isPublic() {
        return boxData == null ? false : boxData.publicBox;
    }

    public boolean isMember(UUID uuid) {
        return boxData == null ? false : boxData.members.contains(uuid);
    }

    public UUID getOwner() {
        return boxData == null ? null : boxData.owner;
    }

    public void setOwner(UUID uuid, String name, boolean forceName) {
        String oldName = name;
        Set<UUID> members = new HashSet<>();
        boolean publicB = false;
        if(boxData != null) {
            members.addAll(boxData.members);
            publicB = boxData.publicBox;
            String n = SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData);
            if(!forceName) {
                oldName = n;
            }
        }
        this.boxData = new ServerBoxData(oldName, new Location(this.getWorld(), this.getPos()), uuid, members, publicB);
        SnailBoxData.getData(this.getWorld().getServer()).addBox(boxData);
        this.markDirty();
    }

    public void addMember(UUID uuid) {
        if(boxData != null) {
            Set<UUID> members = new HashSet<>();
            members.addAll(boxData.members);
            UUID owner = boxData.owner;
            boolean publicB = boxData.publicBox;
            if(!owner.equals(uuid)) {
                members.add(uuid);
            }
            this.boxData = new ServerBoxData(SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData), new Location(this.getWorld(), this.getPos()), owner, members, publicB);
            SnailBoxData.getData(this.getWorld().getServer()).addBox(boxData);
            this.markDirty();
        }
    }

    public void removeMember(UUID uuid) {
        if(boxData != null) {
            Set<UUID> members = new HashSet<>();
            members.addAll(boxData.members);
            members.remove(uuid);
            UUID owner = boxData.owner;
            boolean publicB = boxData.publicBox;
            this.boxData = new ServerBoxData(SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData), new Location(this.getWorld(), this.getPos()), owner, members, publicB);
            SnailBoxData.getData(this.getWorld().getServer()).addBox(boxData);
            this.markDirty();
        }
    }

    public void setPublic(boolean publicB) {
        if(boxData != null) {
            boxData.publicBox = publicB;
            SnailBoxData.getData(this.getWorld().getServer()).setPublic(boxData.pos, publicB);
            this.markDirty();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.getOwner() != null && !this.getWorld().isRemote) {
            if(!SnailBoxData.getData(this.getWorld().getServer()).getBoxes(this.getOwner()).contains(new Location(this.getWorld(), this.getPos()))) {
                SnailBoxData.getData(this.getWorld().getServer()).addBox(this.boxData);
            }
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

    public void setName(String name) {
        if(boxData != null) {
            this.boxData.name = name;
        }
        SnailBoxData.getData(this.getWorld().getServer()).setNameForPos(new Location(this.getWorld(), this.getPos()), name);
        this.markDirty();
    }

    @Override
    public void setWorldAndPos(World newWorld, BlockPos newPos) {
        if(pos != null && world != null && newWorld != world && newPos != pos) {
            if(boxData != null) {
                String oldName = boxData.name;
                if(boxData.owner != null) {
                    oldName = SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData);
                }
                boxData = new ServerBoxData(oldName, new Location(newWorld, newPos), boxData.owner, boxData.members, boxData.publicBox);
            }
            super.setWorldAndPos(newWorld, newPos);
            if(boxData != null) {
                SnailBoxData.getData(this.getWorld().getServer()).addBox(boxData);
            }
        } else {
            super.setWorldAndPos(newWorld, newPos);
        }
    }

    @Override
    public void setPos(BlockPos newPos) {
        if(pos != null && world != null && newPos != pos) {
            if(boxData != null) {
                String oldName = boxData.name;
                if(boxData.owner != null) {
                    oldName = SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData);
                }
                boxData = new ServerBoxData(oldName, new Location(this.getWorld(), newPos), boxData.owner, boxData.members, boxData.publicBox);
            }
            super.setPos(newPos);
            if(boxData != null) {
                SnailBoxData.getData(this.getWorld().getServer()).addBox(boxData);
            }
        } else {
            super.setPos(newPos);
        }
    }

    @Override
    public void updateContainingBlockInfo() {
        // remove if air
        if(boxData != null && !this.getWorld().isRemote && world.isBlockPresent(this.getPos()) && world.getBlockState(this.getPos()).getBlock() != ModBlocks.SNAIL_BOX) {
            SnailBoxData.getData(this.getWorld().getServer()).removeBox(boxData);
        }
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.handler.deserializeNBT(compound.getCompound(ITEM_TAG_KEY));
        this.boxData = ServerBoxData.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.put(ITEM_TAG_KEY, this.handler.serializeNBT());
        if(boxData != null) {
            boxData.write(compound);
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
        NetworkHooks.openGui(player, namedProvider, buf -> {
            String name = SnailBoxData.getData(player.getServer()).getNameForPos(new Location(this.getWorld(), this.getPos()));
            if(name == null) {
                name = "";
            }
            buf.writeString(name, 35);
            buf.writeBoolean(PlayerEntity.getUUID(player.getGameProfile()).equals(this.getOwner()));
            buf.writeBoolean(this.isPublic());
            if(boxData == null) {
                buf.writeInt(0);
            } else {
                Set<String> usernames = new HashSet<String>();
                for(UUID member : this.boxData.members) {
                    GameProfile profile = player.getServer().getPlayerProfileCache().getProfileByUUID(member);
                    if(profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                        usernames.add(profile.getName());
                    } else {
                        usernames.add(member.toString());
                    }
                }
                buf.writeInt(usernames.size());
                for(String username : usernames) {
                    buf.writeString(username);
                }
            }
        });
    }

    public static class SnailBoxContainer extends Container {

        public static SnailBoxContainer getClientContainer(int id, PlayerInventory playerInventory, PacketBuffer extra) {
            if(extra.readableBytes() > 0) {
                String name = extra.readString(35);
                boolean isOwner = extra.readBoolean();
                boolean isPublic = extra.readBoolean();
                int len = extra.readInt();
                Set<String> usernames = new HashSet<String>();
                for(int i = 0; i < len; i++) {
                    usernames.add(extra.readString(35));
                }
                return new SnailBoxContainer(id, playerInventory, new ItemStackHandler(SLOT_COUNT), name, isOwner, isPublic, usernames);
            }
            return new SnailBoxContainer(id, playerInventory, new ItemStackHandler(SLOT_COUNT), "", false, false, new HashSet<>());
        }

        public static IContainerProvider getServerContainerProvider(SnailBoxBlockEntity te) {
            return (id, playerInventory, serverPlayer) -> new SnailBoxContainer(id, playerInventory, te.getPos(), te.getItemHandler());
        }

        private final BlockPos pos;
        private final ItemStackHandler items;
        public String startingName;
        public boolean isOwner = false;
        public boolean isPublic = false;
        public Set<String> memberUsers = new HashSet<String>();

        public SnailBoxContainer(int id, IInventory playerInventory, BlockPos pos, ItemStackHandler items) {
            super(ModContainers.SNAIL_BOX, id);
            this.items = items;
            this.pos = pos;
            this.startingName = "";
            addOwnSlots();
            addPlayerSlots(playerInventory);
        }

        public SnailBoxContainer(int id, IInventory playerInventory, ItemStackHandler items, String startingName, boolean isOwner, boolean isPublic, Set<String> members) {
            super(ModContainers.SNAIL_BOX, id);
            this.items = items;
            this.pos = BlockPos.ZERO;
            this.startingName = startingName;
            this.isOwner = isOwner;
            this.isPublic = isPublic;
            this.memberUsers = members;
            addOwnSlots();
            addPlayerSlots(playerInventory);
        }

        private void addPlayerSlots(IInventory playerInventory) {
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

        private void addOwnSlots() {
            for(int i = 0; i < 27; i++) {
                int yCoord = i / 9 * 18; // 9 slots fit per row, 18 is size of the slot texture
                int xCoord = i % 9 * 18; // 0, 1*18, 2*18, 3*18, loop per row
                this.addSlot(new ReadOnlySlot(items, i, 8 + xCoord, 26 + yCoord));
            }
            this.addSlot(new EnvelopeSlot(items, 27, 8 + 8 * 18, 6));
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
            return isWithinUsableDistance(IWorldPosCallable.of(player.world, pos), player, ModBlocks.SNAIL_BOX) && SnailBoxBlock.canOpen(player.world, pos, player);
        }

        public SnailBoxBlockEntity getTile(PlayerEntity player) {
            TileEntity te = player.world.getTileEntity(pos);
            if(te != null && canInteractWith(player) && player.world.getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX && te instanceof SnailBoxBlockEntity) {
                return ((SnailBoxBlockEntity) te);
            }
            return null;
        }
    }

    public Location getLocation() {
        return new Location(this.getWorld(), this.getPos());
    }

}
