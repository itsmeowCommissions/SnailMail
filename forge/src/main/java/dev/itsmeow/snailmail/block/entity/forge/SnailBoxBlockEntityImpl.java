package dev.itsmeow.snailmail.block.entity.forge;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import dev.itsmeow.snailmail.menu.forge.SnailBoxMenuForge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

public class SnailBoxBlockEntityImpl {

    private static final String ITEM_TAG_KEY = "item_handler";

    public static void loadStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {
        ((SnailBoxInterfaceForge) blockEntity).deserializeHandlerNBT(compoundTag.getCompound(ITEM_TAG_KEY));
    }

    public static void saveStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {
        compoundTag.put(ITEM_TAG_KEY, ((SnailBoxInterfaceForge) blockEntity).serializeHandlerNBT());
    }

    public static void handleRemoved(SnailBoxBlockEntity blockEntity) {
        ((SnailBoxInterfaceForge) blockEntity).handleRemoved();
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
            return new SnailBoxMenuForge(id, playerInventory, new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT), pos, name, isOwner, isPublic, usernames);
        }
        return new SnailBoxMenuForge(id, playerInventory, new ItemStackHandler(SnailBoxBlockEntity.SLOT_COUNT), pos, "", false, false, new HashSet<>());
    }

    public static MenuConstructor getServerMenuProvider(SnailBoxBlockEntity te) {
        return (id, playerInventory, serverPlayer) -> new SnailBoxMenuForge(id, playerInventory, te.getBlockPos(), ((SnailBoxInterfaceForge) te).getItemHandler());
    }

    public static void dropItems(SnailBoxBlockEntity blockEntity) {
        for(int i = 0; i < ((SnailBoxInterfaceForge) blockEntity).getItemHandler().getSlots(); ++i) {
            Containers.dropItemStack(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), ((SnailBoxInterfaceForge) blockEntity).getItemHandler().getStackInSlot(i));
        }
    }

    public static ItemStack getEnvelope(SnailBoxBlockEntity blockEntity) {
        return ((SnailBoxInterfaceForge) blockEntity).getItemHandler().getStackInSlot(27);
    }

    public static void setEnvelope(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        ((SnailBoxInterfaceForge) blockEntity).getItemHandler().setStackInSlot(27, stack);
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
        LazyOptional<IItemHandler> hOpt = te.getCapability(ForgeCapabilities.ITEM_HANDLER);
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
