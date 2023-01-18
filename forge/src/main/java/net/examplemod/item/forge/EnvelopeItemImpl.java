package net.examplemod.item.forge;

import net.examplemod.menu.forge.EnvelopeMenuForge;
import net.examplemod.init.ModItems;
import net.examplemod.item.EnvelopeItem;
import net.examplemod.menu.EnvelopeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Optional;

public class EnvelopeItemImpl {
    public static boolean isStamped(ItemStack stack) {
        LazyOptional<IItemHandler> cap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if(cap.isPresent()) {
            ItemStack stampSlot = cap.orElse(null).getStackInSlot(27);
            if(!stampSlot.isEmpty() && stampSlot.getItem() == ModItems.STAMP.get()) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ItemStack> doConvert(ItemStack stack, boolean fromOpen) {
        LazyOptional<IItemHandler> hOpt = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if(hOpt.isPresent()) {
            IItemHandler handler = hOpt.orElse(null);
            ItemStack newStack = new ItemStack(fromOpen ? ModItems.ENVELOPE_CLOSED.get() : ModItems.ENVELOPE_OPEN.get());
            LazyOptional<IItemHandler> nHOpt = newStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if(nHOpt.isPresent()) {
                IItemHandler newHandler = nHOpt.orElse(null);
                if(newHandler instanceof ItemStackHandler) {
                    ItemStackHandler newH = (ItemStackHandler) newHandler;
                    for(int i = 0; i < Math.min(newH.getSlots(), handler.getSlots()); i++) {
                        newH.setStackInSlot(i, handler.getStackInSlot(i));
                    }
                    copyTagString(stack, newStack, "AddressedTo");
                    copyTagString(stack, newStack, "AddressedFrom");
                    return Optional.of(newStack);
                }
            }
        }
        return Optional.empty();
    }

    protected static void copyTagString(ItemStack original, ItemStack newStack, String key) {
        if(original.hasTag() && original.getTag().contains(key, Tag.TAG_STRING)) {
            EnvelopeItem.putStringChecked(newStack, key, original.getTag().getString(key));
        }
    }

    public static boolean hasItems(ItemStack stack) {
        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) != null;
    }

    public static EnvelopeMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        if(extra.readableBytes() > 0) {
            try {
                BlockPos returnPos = extra.readBlockPos();
                String toName = "";
                if(extra.readableBytes() > 0) {
                    toName = extra.readUtf(35);
                }
                String fromName = "";
                if(extra.readableBytes() > 0) {
                    fromName = extra.readUtf(35);
                }
                return new EnvelopeMenuForge(id, playerInventory, new ItemStackHandler(EnvelopeItem.SLOT_COUNT), returnPos, toName, fromName);
            } catch(IndexOutOfBoundsException e) {
            }
        }
        return new EnvelopeMenuForge(id, playerInventory, new ItemStackHandler(EnvelopeItem.SLOT_COUNT));
    }

    public static MenuConstructor getServerMenuProvider(ItemStack stack) {
        return (id, playerInventory, serverPlayer) -> new EnvelopeMenuForge(id, playerInventory, stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null));
    }

    public static void emptyEnvelope(ItemStack stack, Player playerIn) {
        stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent((inv) -> {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemHandlerHelper.giveItemToPlayer(playerIn, inv.getStackInSlot(i));
            }
        });
    }
}
