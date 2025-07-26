package dev.itsmeow.snailmail.block.entity.neoforge;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

public class SnailBoxBlockEntityImpl {

    private static final String ITEM_TAG_KEY = "item_handler";

    public static void loadStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag, HolderLookup.Provider provider) {
        ((SnailBoxInterfaceNeoForge) blockEntity).deserializeHandlerNBT(compoundTag.getCompound(ITEM_TAG_KEY), provider);
    }

    public static void saveStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.put(ITEM_TAG_KEY, ((SnailBoxInterfaceNeoForge) blockEntity).serializeHandlerNBT(provider));
    }

    public static void handleRemoved(SnailBoxBlockEntity blockEntity) {
        ((SnailBoxInterfaceNeoForge) blockEntity).handleRemoved();
    }

    public static SnailBoxMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        BlockPos pos = extra.readBlockPos();
        if(extra.readableBytes() > 0) {
            String name = extra.readUtf(35);
            boolean isOwner = extra.readBoolean();
            boolean isPublic = extra.readBoolean();
            int len = extra.readInt();
            Set<String> usernames = new HashSet<String>();
            for(int i = 0; i < len; i++) {
                usernames.add(extra.readUtf(35));
            }
            return new dev.itsmeow.snailmail.menu.neoforge.SnailBoxMenuNeoForge(id, playerInventory, new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT), pos, name, isOwner, isPublic, usernames);
        }
        return new dev.itsmeow.snailmail.menu.neoforge.SnailBoxMenuNeoForge(id, playerInventory, new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT), pos, "", false, false, new HashSet<>());
    }

    public static MenuConstructor getServerMenuProvider(SnailBoxBlockEntity te) {
        return (id, playerInventory, serverPlayer) -> new dev.itsmeow.snailmail.menu.neoforge.SnailBoxMenuNeoForge(id, playerInventory, te.getBlockPos(), ((SnailBoxInterfaceNeoForge) te).getItemHandler());
    }

    public static void dropItems(SnailBoxBlockEntity blockEntity) {
        for(int i = 0; i < ((SnailBoxInterfaceNeoForge) blockEntity).getItemHandler().getSlots(); ++i) {
            Containers.dropItemStack(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), ((SnailBoxInterfaceNeoForge) blockEntity).getItemHandler().getStackInSlot(i));
        }
    }

    public static ItemStack getEnvelope(SnailBoxBlockEntity blockEntity) {
        return ((SnailBoxInterfaceNeoForge) blockEntity).getItemHandler().getStackInSlot(27);
    }

    public static void setEnvelope(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        ((SnailBoxInterfaceNeoForge) blockEntity).getItemHandler().setStackInSlot(27, stack);
    }

    public static boolean setEnvelopeServer(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        LazyOptional<IItemHandler> hOpt = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (hOpt.isPresent()) {
            IItemHandler handlerRaw = hOpt.orElse(null);
            if (handlerRaw instanceof ItemStackHandler) {
                ItemStackHandler handler = (ItemStackHandler) handlerRaw;
                handler.setStackInSlot(27, stack);
                return true;
            }
        }
        return false;
    }

    public static boolean hasCapability(SnailBoxBlockEntity te) {
        return getCapability(te) != null;
    }

    private static ItemStackHandler getCapability(SnailBoxBlockEntity te) {
        LazyOptional<IItemHandler> hOpt = te.getCapabilitity(Capabilities.ItemHandler.ITEM);
        if (hOpt.isPresent()) {
            if (hOpt.orElse(null) instanceof ItemStackHandler) {
                return (ItemStackHandler) hOpt.orElse(null);
            }
        }
        return null;
    }

    public static boolean tryInsert(SnailBoxBlockEntity te, ItemStack newEnvelope) {
        ItemStackHandler handler = getCapability(te);
        ItemStack result = newEnvelope;
        for(int i = 0; i < 27 && !result.isEmpty(); i++) {
            result = handler.insertItem(i, newEnvelope, true);
        }
        if(result.isEmpty()) {
            result = newEnvelope;
            for(int i = 0; i < 27 && !result.isEmpty(); i++) {
                result = handler.insertItem(i, newEnvelope, false);
            }
            return true;
        }
        return false;
    }
}
